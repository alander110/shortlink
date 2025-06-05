package com.xw.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xw.shortlink.project.dao.entity.ShortLinkDO;
import com.xw.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.xw.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.xw.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.xw.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.xw.shortlink.project.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);


    /**
     * 恢复短链接
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);

    /**
     * 移除短链接
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
