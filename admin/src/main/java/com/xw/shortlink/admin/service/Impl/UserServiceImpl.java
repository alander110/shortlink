package com.xw.shortlink.admin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xw.shortlink.admin.common.convention.exception.ClientException;
import com.xw.shortlink.admin.common.convention.exception.ServiceException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.shortlink.admin.common.convention.result.Result;
import com.xw.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.xw.shortlink.admin.common.user.UserContext;
import com.xw.shortlink.admin.dao.entity.UserDO;
import com.xw.shortlink.admin.dao.mapper.UserMapper;
import com.xw.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xw.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xw.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xw.shortlink.admin.dto.resp.UserActualRespDTO;
import com.xw.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xw.shortlink.admin.dto.resp.UserRespDTO;
import com.xw.shortlink.admin.service.GroupService;
import com.xw.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.xw.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.xw.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.xw.shortlink.admin.common.enums.UserErrorCodeEnum.*;

/**
 * 用户接口实现层
 *
 * @author alander
 * @date 2025/06/04
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;
    /**
     * 通过用户名获取
     *
     * @param username 用户名
     * @return {@link UserRespDTO }
     */
    @Override
    public UserRespDTO getByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername,username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if(userDO==null){
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(userDO,userRespDTO);
        return userRespDTO;
    }

    /**
     * 用户名是否存在
     *
     * @param username 用户名
     * @return {@link Boolean }
     */
    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO userRegisterReqDTO) {
        //1、如果用户存在，抛异常
        if(hasUsername((userRegisterReqDTO.getUsername()))){
            throw new ServiceException(USER_EXIST);
        }
        //2、分布式锁
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + userRegisterReqDTO.getUsername());
        //2.1、加锁失败，抛异常
        if (!lock.tryLock()) {
            throw new ClientException(USER_NAME_EXIST);
        }
        //2.2、添加用户
        try{
            UserDO userDO = BeanUtil.toBean(userRegisterReqDTO, UserDO.class);
            int inserted = baseMapper.insert(userDO);
            if (inserted < 1) {
                throw new ClientException(USER_SAVE_ERROR);
            }
            //2.2、添加默认分组
            groupService.saveGroup(userRegisterReqDTO.getUsername(),"默认分组");
            //2.3、添加到布隆过滤器
            userRegisterCachePenetrationBloomFilter.add(userRegisterReqDTO.getUsername());
        } catch (Exception e) {
            throw new ClientException(USER_EXIST);
        }finally {
            //解锁
            lock.unlock();
        }


    }

    /**
     * 修改用户
     *
     * @param userUpdateReqDTO 用户更新REQ DTO
     */
    @Override
    public void updateUser(UserUpdateReqDTO userUpdateReqDTO) {
        //1、判断当前用户和传递的参数是否一致
        if(!Objects.equals(userUpdateReqDTO.getUsername(), UserContext.getUsername())){
            throw new ClientException("当前登录用户修改请求异常");
        }
        //2、修改用户信息
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername,userUpdateReqDTO.getUsername());
        UserDO userDO = BeanUtil.toBean(userUpdateReqDTO, UserDO.class);
        baseMapper.update(userDO,updateWrapper);
    }

    /**
     * 登录
     *
     * @param userLoginReqDTO 用户登录DTO
     * @return {@link UserLoginReqDTO }
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername,userLoginReqDTO.getUsername())
                .eq(UserDO::getPassword,userLoginReqDTO.getPassword())
                .eq(UserDO::getDelFlag,0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if(userDO == null){
            throw new ClientException("用户不存在");
        }
        //缓存到redis
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + userLoginReqDTO.getUsername());
        if(CollUtil.isNotEmpty(hasLoginMap)){
            //如果redis已经存在，重新设置过期时间
            stringRedisTemplate.expire(USER_LOGIN_KEY+userLoginReqDTO.getUsername(),30L, TimeUnit.MINUTES);
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDTO(token);
        }
        //如果不存在，生成token并保持到redis
        /**
         * Hash
         * Key：login_用户名
         * Value：
         *  Key：token标识
         *  Val：JSON 字符串（用户信息）
         */
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY+userLoginReqDTO.getUsername(),uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY+userLoginReqDTO.getUsername(),30L,TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    /**
     * 检测是否登录
     * @param username 用户名
     * @param token
     * @return {@link Result }<{@link Boolean }>
     */
    @Override
    public Boolean checkLogin(String username, String token) {
         return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username,token) !=null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }

}
