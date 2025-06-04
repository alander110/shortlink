package com.xw.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xw.shortlink.admin.common.convention.result.Result;
import com.xw.shortlink.admin.dao.entity.UserDO;
import com.xw.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xw.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xw.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xw.shortlink.admin.dto.resp.UserActualRespDTO;
import com.xw.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xw.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO> {

    /**
     * 通过用户名获取
     *
     * @param username 用户名
     * @return {@link UserRespDTO }
     */
    UserRespDTO getByUsername(String username);

    /**
     * 用户名是否存在
     *
     * @param username 用户名
     * @return {@link Boolean }
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     *
     * @param userRegisterReqDTO 用户做
     */
    void register(UserRegisterReqDTO userRegisterReqDTO);


    /**
     * 修改用户
     *
     * @param userUpdateReqDTO 用户更新REQ DTO
     */
    void updateUser(UserUpdateReqDTO userUpdateReqDTO);


    /**
     * 登录
     *
     * @param  userLoginReqDTO 用户登录DTO
     * @return {@link UserLoginReqDTO }
     */
    UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO);

    /**
     * 检测是否登录
     * @param username 用户名
     * @param token
     * @return {@link Result }<{@link Boolean }>
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登录
     * @param username 用户名
     * @param token
     * @return {@link Result }<{@link Void }>
     */
    void logout(String username, String token);
}
