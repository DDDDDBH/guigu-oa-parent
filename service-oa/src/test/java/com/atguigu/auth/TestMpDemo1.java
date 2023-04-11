package com.atguigu.auth;


import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.model.system.SysRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TestMpDemo1 {
    @Autowired
    private SysRoleMapper mapper;
    //查询所有记录
    @Test
    public void getAll() {
        System.out.println(mapper.selectList(null));
    }
    @Test
    public void testInsert() {
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");

        int result = mapper.insert(sysRole);
        System.out.println(result); //影响的行数
        System.out.println(sysRole.getId()); //id自动回填
    }

    @Test
    public void testUpdate() {
        SysRole sysRole = mapper.selectById(10);
        sysRole.setRoleName("角色管理员1");
        int update = mapper.updateById(sysRole);
        System.out.println(update);
    }

    @Test
    public void testDelete() {
        int result = mapper.deleteById(10L);
    }
    @Test
    public void testDeleteBatchIds() {
        int result = mapper.deleteBatchIds(Arrays.asList(1, 1));
        System.out.println(result);
    }

    @Test
    public void testQuery() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SysRole::getRoleName, "总经理");
        List<SysRole> sysRoles = mapper.selectList(wrapper);
        System.out.println(sysRoles);
    }

    @Autowired
    private SysRoleService sysRoleService;

    @Test
    public void testSelectList() {
        System.out.println(("----- selectAll method test ------"));
        //UserMapper 中的 selectList() 方法的参数为 MP 内置的条件封装器 Wrapper
        //所以不填写就是无任何条件
        List<SysRole> users = sysRoleService.list();
        users.forEach(System.out::println);
    }


}
