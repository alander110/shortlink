package com.xw.shortlink.admin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.shortlink.admin.common.convention.exception.ClientException;
import com.xw.shortlink.admin.common.convention.exception.ServiceException;
import com.xw.shortlink.admin.common.convention.result.Result;
import com.xw.shortlink.admin.common.database.BaseDO;
import com.xw.shortlink.admin.common.user.UserContext;
import com.xw.shortlink.admin.dao.entity.GroupDO;
import com.xw.shortlink.admin.dao.entity.GroupUniqueDO;
import com.xw.shortlink.admin.dao.mapper.GroupMapper;
import com.xw.shortlink.admin.dao.mapper.GroupUniqueMapper;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.xw.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.xw.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.xw.shortlink.admin.service.GroupService;
import com.xw.shortlink.admin.toolkit.RandomGenerator;
import jdk.jfr.consumer.RecordedStackTrace;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jcajce.provider.symmetric.SEED;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.xw.shortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;


@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final RBloomFilter<String> gidRegisterCachePenetrationBloomFilter;
    private final GroupUniqueMapper groupUniqueMapper;
    private final RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Integer groupMaxNum;

    /**
     * 新增短链接分组
     */
    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(),groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try{
            //2、查询用户所有分组
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
            //3.1如果用户分组数量超出最大限制，抛出
            if(CollUtil.isNotEmpty(groupDOList) && groupDOList.size() >= groupMaxNum){
                throw new ClientException(String.format("超出最大分组数：%d",groupMaxNum));
            }
            //3.2不断尝试saveGroupUniqueReturnGid直到最大次数
            int retryCount = 0;
            int maxRetries = 10;
            String gid = null;
            while (retryCount < maxRetries) {
                gid = saveGroupUniqueReturnGid();
                if (StrUtil.isNotEmpty(gid)) {
                    GroupDO groupDO = GroupDO.builder()
                            .gid(gid)
                            .sortOrder(0)
                            .username(username)
                            .name(groupName)
                            .build();
                    baseMapper.insert(groupDO);
                    gidRegisterCachePenetrationBloomFilter.add(gid);
                    break;
                }
                retryCount++;
            }
            if(StrUtil.isEmpty(gid)){
                throw new ServiceException("生成分组表示频繁");
            }
        }finally {
            //4解锁
            lock.unlock();
        }
    }

    /**
     * 生成组id并保存到groupUnique表中
     * @return {@link String }
     */
    private String saveGroupUniqueReturnGid(){
        String gid = RandomGenerator.generateRandom();
        if(gidRegisterCachePenetrationBloomFilter.contains(gid)){
            return null;
        }
        GroupUniqueDO groupUniqueDO = GroupUniqueDO.builder()
                .gid(gid)
                .build();
        try{
            groupUniqueMapper.insert(groupUniqueDO);
        }catch (DuplicateKeyException e){
            return null;
        }
        return gid;
    }

    /**
     * 查询短链接分组集合
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag,0)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder,GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
        //TODO 查询分组短链接数量，填充shortLinkGroupRespDTOList
        return shortLinkGroupRespDTOList;
    }


    /**
     * 修改短链接分组名称
     */
    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO shortLinkGroupUpdateReqDTO) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .eq(GroupDO::getGid,shortLinkGroupUpdateReqDTO.getGid())
                .eq(GroupDO::getDelFlag,0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(shortLinkGroupUpdateReqDTO.getName());
        baseMapper.update(groupDO,updateWrapper);
    }

    /**
     * 删除短链接分组
     */
    @Override
    public void deleteGroup(String gid) {
       LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
               .eq(GroupDO::getUsername,UserContext.getUsername())
               .eq(GroupDO::getGid,gid)
               .eq(GroupDO::getDelFlag,0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO,updateWrapper);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> shortLinkGroupSortReqDTOS) {
        shortLinkGroupSortReqDTOS.forEach(each->{
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername,UserContext.getUsername())
                    .eq(GroupDO::getGid,each.getGid())
                    .eq(BaseDO::getDelFlag,0);
            baseMapper.update(groupDO,updateWrapper);
        });
    }

}
