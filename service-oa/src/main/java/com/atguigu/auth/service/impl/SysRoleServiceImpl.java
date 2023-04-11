package com.atguigu.auth.service.impl;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.mapper.SysUserRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.auth.service.SysUserRoleService;
import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUser;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    //查询所有角色 和 当前用户所属角色
    @Override
    public Map<String, Object> findRoleByAdminId(Long userId) {
        //1.查询所有角色
        List<SysRole> allRoleList =
                baseMapper.selectList(null);

        //2.根据用户id查询用户关系表 ， 查询用户id对应角色id
        LambdaQueryWrapper <SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> existRoleList = sysUserRoleService.list(queryWrapper);
       // List<Long> existRoleIdList = existRoleList.stream().map(c -> c.getRoleId()).collect(Collectors.toList());
        List<Long> list = new ArrayList<>();
        for (SysUserRole sys:
        existRoleList) {
            Long roleId = sys.getRoleId();
            list.add(roleId);
        }


        //3.根据角色id查询角色表，查询角色信息
        //根据角色id到所有角色的list集合进行比较
        List<SysRole> assignRoleList = new ArrayList<>();
        for (SysRole sysRole : allRoleList){
            //比较
            if(list.contains(sysRole.getId())){
                assignRoleList.add(sysRole);
            }
        }
        //4.将得到的两部分数据封装到map集合中，返回
        Map<String , Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList" , assignRoleList);
        roleMap.put("allRolesList" , allRoleList);
        return roleMap;
    }

    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        //把之前用户的数据删除，用户角色关系表里 ，根据用户角色id删除
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysUserRole::getUserId , assginRoleVo.getUserId());
        sysUserRoleService.remove(queryWrapper);

        //重新分配
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for (Long roleId:
        roleIdList) {
            if (StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRole.setRoleId(roleId);
            sysUserRoleService.save(sysUserRole);
        }
    }
}

