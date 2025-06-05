package com.xw.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.xw.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.xw.shortlink.project.service.ShortLinkGotoService;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkGotoServiceImpl extends ServiceImpl<ShortLinkGotoMapper, ShortLinkGotoDO> implements ShortLinkGotoService {

    @Override
    public void insert(ShortLinkGotoDO linkGotoDO) {
        baseMapper.insert(linkGotoDO);
    }
}
