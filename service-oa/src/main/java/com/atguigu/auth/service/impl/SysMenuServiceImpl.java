package com.atguigu.auth.service.impl;


import com.atguigu.auth.mapper.SysMenuMapper;
import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysRoleMenuService;
import com.atguigu.auth.util.MenuHelper;
import com.atguigu.model.system.SysMenu;
import com.atguigu.model.system.SysRoleMenu;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssignMenuVo;
import com.atguigu.vo.system.MetaVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.StringUtils.*;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-04-06
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    SysRoleMenuService sysRoleMenuService = new SysRoleMenuServiceImpl();

    @Override
    public List<SysMenu> findNodes() {
        //全部权限列表
        List<SysMenu> sysMenuList = this.list();
        if (CollectionUtils.isEmpty(sysMenuList)) return null;

        //构建树形数据
        List<SysMenu> result = MenuHelper.buildTree(sysMenuList);
        return result;
    }

    @Override
    public List<SysMenu> findSysMenuByRoleId(Long roleId) {

        //1 查询所有的菜单
        LambdaQueryWrapper<SysMenu> wrapperSystemMenu = new LambdaQueryWrapper<>();
        wrapperSystemMenu.eq(SysMenu::getStatus , 1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(wrapperSystemMenu);


        //2 根据角色Id roleId 查询角色菜单表里面的 角色Id对应所有菜单Id
        LambdaQueryWrapper<SysRoleMenu> wrapperSysRoleMenu = new LambdaQueryWrapper<>();
        wrapperSysRoleMenu.eq(SysRoleMenu::getRoleId , roleId);
        List<SysRoleMenu> sysRoleMenuList =
                sysRoleMenuService.list(wrapperSysRoleMenu);


        //3 根据菜单id来获取菜单的菜单对象
        List<Long> sysRoleMenuIdList = new ArrayList<>();
        for (SysRoleMenu sysRoleMenu:
        sysRoleMenuList) {
            Long menuId = sysRoleMenu.getMenuId();
            sysRoleMenuIdList.add(menuId);
        }
        //3.1  拿着菜单id和所有菜单集合里面的id进行比较，如果相同进行封装
        for (SysMenu sys:
                allSysMenuList) {
            if (sysRoleMenuIdList.contains(sys.getId())){
                sys.setSelect(true);
            }else {
                sys.setSelect(false);
            }
        }
        //返回规定格式的菜单列表
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }

    @Override
    public void doAssign(AssignMenuVo assignMenuVo) {
        //1 根据角色Id删除菜单角色表分配数据
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysRoleMenu::getRoleId , assignMenuVo.getRoleId());
        sysRoleMenuService.remove(queryWrapper);
        //2 从参数里面获取决赛角色新分配菜单id列表
        List<Long> menuIdList = assignMenuVo.getMenuIdList();
        for (Long id:
        menuIdList) {
            if (StringUtils.isEmpty(id)){
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(assignMenuVo.getRoleId());
            sysRoleMenu.setMenuId(id);
            sysRoleMenuService.save(sysRoleMenu);
        }
        //进行遍历，把每个id添加菜单角色表
    }

    //4 根据用户id获取用户可以操作的菜单
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList = null;
        //1.1 判断是否是管理员，如果是管理员，
        if(userId.longValue() == 1){
            //查询所有菜单列表
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper();
            wrapper.eq(SysMenu::getStatus , 1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);

        }else {
            //1.2 如果不是管理员，根据UserId查询可以操作按钮列表
            //多表关联查询：用户角色关系表，角色菜单关系表，菜单表
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }

        //2 从查询出来的数据里面，获取可以操作的按钮值的list集合，返回
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        List<RouterVo> routerList = this.buildMenus(sysMenuTreeList);
        return routerList;
    }

    //5 根据用户id获取用户可以操作的按钮


    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        //超级管理员admin账号id为：1
        List<SysMenu> sysMenuList = null;
        if (userId.longValue() == 1) {
            sysMenuList = this.list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1));
        } else {
            sysMenuList = sysMenuMapper.findMenuListByUserId(userId);
        }
        List<String> permsList = sysMenuList.stream().filter(item -> item.getType() == 2).map(item -> item.getPerms()).collect(Collectors.toList());
        return permsList;
    }

    private List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            List<SysMenu> children = menu.getChildren();
            //如果当前是菜单，需将按钮对应的路由加载出来，如：“角色授权”按钮对应的路由在“系统管理”下面
            if(menu.getType().intValue() == 1) {
                List<SysMenu> hiddenMenuList = children.stream().filter(item -> !StringUtils.isEmpty(item.getComponent())).collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            } else {
                if (!CollectionUtils.isEmpty(children)) {
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    router.setChildren(buildMenus(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    @Override
    public boolean removeById(Serializable id) {
        int count = this.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (count > 0) {
            throw new com.atguigu.common.execption.GuiguException(201,"菜单不能删除");
        }
        sysMenuMapper.deleteById(id);
        return false;
    }
}
