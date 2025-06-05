package com.xw.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xw.shortlink.admin.dao.entity.GroupDO;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.xw.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 分组服务层
 * @author alander
 * @date 2025/06/04
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     */
    void saveGroup(String groupName);

    /**
     * 新增短链接分组
     *
     * @param username  用户名
     * @param groupName 短链接分组名
     */
    void saveGroup(String username, String groupName);

    /**
     * 查询短链接分组集合
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组名称
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO shortLinkGroupUpdateReqDTO);


    /**
     * 删除短链接分组
     */
    void deleteGroup(String gid);

    /**
     * 排序短链接分组
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> shortLinkGroupSortReqDTOS);
}
