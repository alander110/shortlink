package com.xw.shortlink.admin.controller;

import com.xw.shortlink.admin.common.convention.result.Result;
import com.xw.shortlink.admin.common.convention.result.Results;
import com.xw.shortlink.admin.dao.entity.UserDO;
import com.xw.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xw.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xw.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xw.shortlink.admin.dto.resp.UserActualRespDTO;
import com.xw.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xw.shortlink.admin.dto.resp.UserRespDTO;

import com.xw.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 *
 * @author alander
 * @date 2025/06/04
 */
@RestController
@RequestMapping("/api/short-link/admin/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;



    /**
     * 通过用户名获取用户
     *
     * @param username 用户名
     * @return {@link Result }<{@link UserRespDTO }>
     */
    @GetMapping("/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username){
        UserRespDTO userRespDTO = userService.getByUsername(username);
        return Results.success(userRespDTO);
    }

    /**
     * 通过用户名获取无脱敏用户信息
     *
     * @param username 用户名
     * @return {@link Result }<{@link UserActualRespDTO }>
     */
    @GetMapping("/actual/user/{username}")
    public Result<UserActualRespDTO> getActualByUsername(@PathVariable String username){
        UserRespDTO userRespDTO = userService.getByUsername(username);
        UserActualRespDTO result = new UserActualRespDTO();
        BeanUtils.copyProperties(userRespDTO,result);
        return Results.success(result);
    }

    /**
     * 查询用户名是否存在
     *
     * @param username 用户名
     * @return 用户名存在返回 True，不存在返回 False
     */
    @GetMapping("/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username){
        return  Results.success(userService.hasUsername(username));
    }

    /**
     * 用户注册
     *
     * @param userRegisterReqDTO 用户注册req dto
     * @return {@link Result }<{@link Void }>
     */
    @PostMapping("/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO userRegisterReqDTO){
        userService.register(userRegisterReqDTO);
        return Results.success();
    }

    /**
     * 修改用户
     *
     * @param userUpdateReqDTO 用户更新REQ DTO
     * @return {@link Result }<{@link Void }>
     */
    @PutMapping("/user")
    public Result<Void> updateUser(@RequestBody UserUpdateReqDTO userUpdateReqDTO){
        userService.updateUser(userUpdateReqDTO);
        return Results.success();
    }

    /**
     * 用户登录
     *
     * @param userLoginReqDTO 用户登录DTO
     * @return {@link Result }<{@link UserLoginReqDTO }>
     */
    @PostMapping("/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO userLoginReqDTO){
        UserLoginRespDTO result=userService.login(userLoginReqDTO);
        return Results.success(result);
    }

    /**
     * 检测是否登录
     * @param username 用户名
     * @param token
     * @return {@link Result }<{@link Boolean }>
     */
    @GetMapping("/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token){
        return Results.success(userService.checkLogin(username,token));
    }

    /**
     * 退出登录
     * @param username 用户名
     * @param token
     * @return {@link Result }<{@link Void }>
     */
    @DeleteMapping("/user/logout")
    public Result<Void> logout(@RequestParam("username") String username, @RequestParam("token") String token) {
        userService.logout(username, token);
        return Results.success();
    }

}
