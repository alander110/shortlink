package com.xw.shortlink.admin.controller;

import com.xw.shortlink.admin.common.convention.result.Result;
import com.xw.shortlink.admin.common.convention.result.Results;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.xw.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.xw.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.xw.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分组控制器
 * @author alander
 * @date 2025/06/04
 */
@RestController
@RequestMapping("/api/short-link/admin/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增短链接分组
     */
    @PostMapping
    public Result<Void> save(@RequestBody ShortLinkGroupRespDTO shortLinkGroupRespDTO){
        groupService.saveGroup(shortLinkGroupRespDTO.getName());
        return Results.success();

    }


    /**
     * 查询短链接分组集合
     */
    @GetMapping
    public Result<List<ShortLinkGroupRespDTO>> listGroup(){
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名称
     */
    @PutMapping
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO shortLinkGroupUpdateReqDTO){
        groupService.updateGroup(shortLinkGroupUpdateReqDTO);
        return Results.success();
    }


    /**
     * 删除短链接分组
     */
    @DeleteMapping
    public Result<Void> deleteGroup(@RequestParam String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }


    /**
     * 排序短链接分组
     */
    @PostMapping("/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> shortLinkGroupSortReqDTOS){
        groupService.sortGroup(shortLinkGroupSortReqDTOS);
        return Results.success();
    }

}
