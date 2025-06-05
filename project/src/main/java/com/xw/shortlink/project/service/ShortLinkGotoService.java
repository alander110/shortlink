package com.xw.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xw.shortlink.project.dao.entity.ShortLinkGotoDO;

public interface ShortLinkGotoService extends IService<ShortLinkGotoDO> {
    void insert(ShortLinkGotoDO linkGotoDO);
}
