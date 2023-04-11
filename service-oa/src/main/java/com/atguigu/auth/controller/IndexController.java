package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.execption.GuiguException;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.Result;
import com.atguigu.common.utils.MD5;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.LoginVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 后台登录登出
 * </p>
 */
@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
//        Map<String , Object> map = new HashMap<>();
//        map.put("token" , "admin");
//        return Result.ok(map);
        //1 获取输入用户名和密码
        //2 根据用户名来查询数据库
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SysUser::getUsername , username);
        SysUser sysUser = sysUserService.getOne(wrapper);
        if (sysUser == null){
            throw new GuiguException(201,"用户不存在");
        }
        //3判断密码
        //数据库存储的密码（MD5）
        String password_db = sysUser.getPassword();
        String password_input = loginVo.getPassword();
        String encrypt = MD5.encrypt(password_input);
        if (password_db.equals(encrypt)){
            throw new GuiguException(201,"密码错误");
        }

        if (sysUser.getStatus().intValue() == 0){
            throw new GuiguException(201,"用户被禁用");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("token", JwtHelper.createToken(sysUser.getId(), sysUser.getUsername()));
        return Result.ok(map);
    }

    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("info")
    public Result info(HttpServletRequest httpServletRequest) {
        //1 从请求头获取用户信息（获取token字符串）
        String token = httpServletRequest.getHeader("token");
        //2 从token字符串中获取用户id或用户名称
        Long userId = JwtHelper.getUserId(token);

        //3 查询数据库把用户信息查询出来
        SysUser sysUser = sysUserService.getById(userId);

        //4 根据用户id获取用户可以操作的菜单
        //查询数据库动态构建路由结构进行显示
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);
        //5 根据用户id获取用户可以操作的按钮
        List<String> permsList= sysMenuService.findUserPermsByUserId(userId);
        //6 返回相应的数据
        Map<String , Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        //返回用户可以操作菜单
        map.put("routers" , routerList);
        //返回用户可以操作按钮
        map.put("buttons",permsList);
        return Result.ok(map);
    }

    /**
     * 退出
     * @return
     */
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
