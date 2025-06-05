package com.xw.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xw.shortlink.project.dao.entity.ShortLinkDO;
import com.xw.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.xw.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.xw.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.xw.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.xw.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.xw.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.xw.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.xw.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.xw.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 短链接跳转原始链接
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 短链接统计
     */
    void shortLinkStats(ShortLinkStatsRecordDTO shortLinkStatsRecord);

    /**
     * 创建短链接
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 通过分布式锁创建短链接
     */
    ShortLinkCreateRespDTO createShortLinkByLock(ShortLinkCreateReqDTO requestParam);


    /**
     * 批量创建短链接
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 修改短链接
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 分页查询短链接
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询短链接分组内数量
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);


}
