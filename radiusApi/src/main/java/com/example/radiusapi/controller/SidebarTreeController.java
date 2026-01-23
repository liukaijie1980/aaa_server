package com.example.radiusapi.controller;

import com.example.radiusapi.entity.SidebarTree;

import com.example.radiusapi.mapper.AccountToNodeMapper;
import com.example.radiusapi.mapper.SidebarTreeMapper;

import com.example.radiusapi.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class SidebarTreeController {
    @Autowired
    private SidebarTreeMapper treeNodeMapper;

    @Autowired
    private AccountToNodeMapper accountToNodeMapper;

     //递归遍历出给定List中指定id的所有子list，
    private  List<SidebarTree> getAllSubNodes(List<SidebarTree> menuList, String id, List<SidebarTree> childList)
    {
        for (int i=0;i<menuList.size();i++) {
            //遍历出父id等于参数的id,add进子节点集合
            if (menuList.get(i).getPid().equals(id) ) {
                childList.add(menuList.get(i));
                String subid=menuList.get(i).getId();
                //每次递归都要全数组遍历，如果数据量大，应该用链表实现本函数，在递归前去掉已经处理过的元素
                //LinkList.remove(menuList.get(i));
                //递归遍历下一级
                getAllSubNodes(menuList, subid,childList);

            }
        }
        return childList;
    }

//上述函数的非递归实现版本
private List<SidebarTree> getAllSubNodes(List<SidebarTree> menuList, String id) {
    List<SidebarTree> childList = new ArrayList<>();
    Map<String, List<SidebarTree>> parentChildMap = new HashMap<>();

    // 创建一个父子关系的Map
    for (SidebarTree node : menuList) {
        parentChildMap
                .computeIfAbsent(node.getPid(), k -> new ArrayList<>())
                .add(node);
    }

    // 使用队列迭代地获取所有子节点
    Queue<String> queue = new LinkedList<>();
    queue.add(id);

    while (!queue.isEmpty()) {
        String currentId = queue.poll();
        List<SidebarTree> children = parentChildMap.get(currentId);

        if (children != null) {
            for (SidebarTree child : children) {
                childList.add(child);
                queue.add(child.getId());
            }
        }
    }

    return childList;
}


    public String findRootNodeId(List<SidebarTree> trlist) throws Exception {
        // 创建一个用于保存所有节点 ID 的集合
        List<String> allIds = trlist.stream().map(SidebarTree::getId).collect(Collectors.toList());

        // 找出一个节点，其 pid 没有在 allIds 中出现
        Optional<SidebarTree> rootNode = trlist.stream().filter(node ->
                !allIds.contains(node.getPid())
        ).findFirst();

        // 如果找到了根节点，返回其 ID
        if (rootNode.isPresent()) {
            return rootNode.get().getId();
        }

        // 如果没有找到，抛出异常
        throw new Exception("Root node not found in the provided list.");
    }

    @Operation(summary ="Get SidebarTree")
    @GetMapping("/SidebarTree")
    public Result GetTreeNodeList( @RequestParam("nodeid") String NodeId)
    {
        log.info("GetTreeNodeList({})", NodeId);

       // QueryWrapper<SidebarTree> wrapper= new QueryWrapper<SidebarTree>();
       // wrapper.orderByAsc("label");
        List<SidebarTree> nodeList;
        Result ret=new Result();
        try {
            //获取数据库中关于前端左侧树节点的所有记录
            nodeList = treeNodeMapper.selectList(null);

        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return ret;
        }
        log.debug(nodeList.toString());

        List<SidebarTree> childList=new ArrayList<>();
        if (nodeList.size()>0)
        {
           log.debug("getAllSubNodes->childList" );

            //取得NodeId节点的所有孩子节点
            //childList=getAllSubNodes(nodeList,NodeId,childList);  //递归版本，已淘汰
            childList=getAllSubNodes(nodeList,NodeId);
            //加上NodeId节点自己
            for (SidebarTree mu:nodeList) {
               if ( mu.getId().equals(NodeId))
               {
                   childList.add(mu);
               }
            }
        }
        ret=new Result();
        ret.ok();

        ret.data("data",childList);
        // ret.data("data",nodeList);
        log.info("result={}",ret);
        return  ret;
    }


@Operation(summary ="set SidebarTree")
@PostMapping("/SidebarTree")
@Transactional // 如果任何操作失败，回滚事务
public Result setTreeNodeList(@RequestBody List<SidebarTree> trlist) {
    try {
        // 从数据库检索所有现有记录
        List<SidebarTree> existingRecords = treeNodeMapper.selectList(null);

        //找出前端根记录的id
        String rootId=findRootNodeId(trlist);

        //用该id找到本地数据库对应的所有子树的记录
        List<SidebarTree>childList=getAllSubNodes(existingRecords,rootId);

        // 更新或添加来自前端的每个节点
        for (SidebarTree frontendNode : trlist) {
            SidebarTree dbNode = treeNodeMapper.selectById(frontendNode.getId());
            if (dbNode == null) {
                // 如果节点在数据库中不存在，添加它
                treeNodeMapper.insert(frontendNode);
            } else {
                // 节点存在，更新它
                dbNode.setLabel(frontendNode.getLabel());
                dbNode.setType(frontendNode.getType());
                dbNode.setOwner(frontendNode.getOwner());
                dbNode.setPid(frontendNode.getPid());
                treeNodeMapper.updateById(dbNode);
            }
        }

        // 删除在对应子树中但不在前端树中的节点
        for (SidebarTree dbNode : childList) {
            if (trlist.stream().noneMatch(node -> node.getId().equals(dbNode.getId()))) {
                treeNodeMapper.deleteById(dbNode.getId());
            }
        }
        Result ret=new Result();
        ret.ok();
        return ret; // 假设 Result 是用于封装您的响应的某个类

    } catch (Exception e) {
        // 重新抛出异常，以便 @Transactional 可以捕获并回滚事务
        throw new RuntimeException("An error occurred while updating the tree: " + e.getMessage(), e);
    }
}

    @Operation(summary ="getNodeIdsByAcccount")
    @GetMapping("/getNodeIdsByAcccount")
    public Result getNodeIdsByAcccount( @RequestParam("username") String  username) {

        log.info("getNodeIdsByAcccount({})", username);
        Result ret=new Result();
        List<String> nodeId;
        try {
            nodeId = accountToNodeMapper.findDistinctNodeIdsByUsername("%" + username + "%");
        }catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return ret;
        }


        ret.ok();

        ret.data("data",nodeId);
        // ret.data("data",nodeList);
        log.info("result={}",ret);
        return  ret;
    }


}

