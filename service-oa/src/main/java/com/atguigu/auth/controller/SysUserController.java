package com.atguigu.auth.controller;


import com.atguigu.auth.service.SysRoleService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.result.Result;
import com.atguigu.common.utils.MD5;
import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.SysRoleQueryVo;
import com.atguigu.vo.system.SysUserQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-04-03
 */
@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/admin/system/sysUser")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    @ApiOperation("用户条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable long page ,
                        @PathVariable long limit ,
                        SysUserQueryVo sysUserQueryVo){
        //定義page
        Page<SysUser> page1 = new Page<>(page , limit);
        //判斷null,如果非空就用Lambd表達式來條件判斷
        String username = sysUserQueryVo.getKeyword();
        String createTimeBegin = sysUserQueryVo.getCreateTimeBegin();
        String createTimeEnd = sysUserQueryVo.getCreateTimeEnd();
        LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<SysUser>();

        if (!StringUtils.isEmpty(username)){
            lqw.like(SysUser::getName , username);
        }

        if(!StringUtils.isEmpty(createTimeBegin)) {
            lqw.ge(SysUser::getCreateTime,createTimeBegin);
        }
        //le 小于等于
        if(!StringUtils.isEmpty(createTimeEnd)) {
            lqw.le(SysUser::getCreateTime,createTimeEnd);
        }
        //用service MP功能分頁
        Page<SysUser> page2 = sysUserService.page(page1, lqw);
        return Result.ok(page2);
    }

    @ApiOperation(value = "获取用户")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.ok(user);
    }

    @ApiOperation(value = "保存用户")
    @PostMapping("save")
    public Result save(@RequestBody SysUser sysUser) {
        //加密密码用MD5
        String password = sysUser.getPassword();
        String passwordMD5 = MD5.encrypt(password);
        sysUser.setPassword(passwordMD5);
        sysUserService.save(sysUser);
        return Result.ok();
    }


    @ApiOperation(value = "更新用户")
    @PutMapping("update")
    public Result updateById(@RequestBody SysUser sysUser) {
        sysUserService.updateById(sysUser);
        return Result.ok();
    }


    @ApiOperation(value = "删除用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.ok();
    }

    @ApiOperation(value = "更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        sysUserService.updateStatus(id, status);
        return Result.ok();
    }


}

