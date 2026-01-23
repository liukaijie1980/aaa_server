
import { getTreeData,setTreeData } from '@/api/sideBarTree'


//返回输出数组  (list:输入数组，parId:根节点的pid)
function listToTree(list, parId) {
  let obj = {};
  let result = [];
  //将数组中数据转为键值对结构 (这里的数组和obj会相互引用)
  list.map(el => {
    obj[el.id] = el;
  })
  for (let i = 0, len = list.length; i < len; i++) {
    let id = list[i].pid;
    if (id == parId) {
      result.push(list[i]);
      continue;
    }
    if (obj[id].children) {
      obj[id].children.push(list[i]);
    } else {
      obj[id].children = [list[i]];
    }
    // 对children按照label排序
    obj[id].children.sort((a, b) => a.label.localeCompare(b.label));
  }
  return result;
}

function treeToList(tree, childName = 'children') {
  // 设置临时数组，用来存放队列
  let queen = [];
  // 设置输出数组，用来存放要输出的一维数组
  const result = [];
  queen = queen.concat(tree);
  // 对树对象进行广度优先的遍历
  while (queen.length) {
    const first = queen.shift();
    if (first[childName]) {
      queen = queen.concat(first[childName]);
      delete first[childName]
    }
    result.push(first);
  }
  return result;
}

//在树的数组元素里找出根节点元素，并返回该元素的pid
function findPidOfRoot(TreeList)
{  
 
  for(let i=0; i < TreeList.length; i++) {
     //简单认为pid没有对应元素对应的就是根节点    
     if (   undefined=== TreeList.find(element=>element.id==TreeList[i].pid) ){ 
        return  TreeList[i].pid
      }
  }
}

const InitData = [
  {
    id: 2,
    label: "无法连接后端数据",
    isFolder: true,
    pid: 1,
    // children: [{
    //   id: 4,
    //   label: "二级 1-1",
    //   isFolder: true,
    //   pid: 1,

    // }]
  }

]


const state = { 
   
  data:InitData,
  NodeName:InitData,
  treeObj:{},
  rootPid:'',
}

const mutations = {
 //刷新整个树控件的数据
  SET_TREE: (state, Tree) => {
    console.log("mutations SET_TREE",Tree)
    state.data = Tree
  },


//保存树控件对象本身，以供其他模块引用
  SET_TREE_OBJ: (state, treeObj) => {
    console.log("mutations SET_TREE_OBJ",treeObj)
    state.treeObj = treeObj
  },

  //保存树控件对象本身，以供其他模块引用
  SET_ROOT_PID: (state, rootPid) => {
    console.log("mutations SET_ROOTID",rootPid)
    state.rootPid = rootPid
  },
 


}

const actions = {
  SetTree({ commit }, root) {    
    console.log("action:SetTree:state.data=", state.data)
    console.log("action:SetTree:node=", root)
    var dataOfRoot=JSON.parse(JSON.stringify(root.data))
    
    console.log("dataOfRoot",dataOfRoot);
    var r = treeToList(dataOfRoot)
    console.log("action:treeToList=", r)
    setTreeData(r).then(
      Response=>{
        console.log("action:setTree()finished")
      }
    )

    //commit('SET_TREE', data)
  },

  //从服务器获取sidebarTree的节点信息，信息以list形式取得
  GetTree({ commit },nodeid) {
    
    console.log("action:GetTree()starting:",nodeid)
   
    
   getTreeData( nodeid  ).then(    
      Response => {
        console.log("action:GetTree()finished:", Response.data.data)   
        
        //找出根节点的pid
        let root=findPidOfRoot(Response.data.data)
        console.log("root=",root)
      
        //本地调用不能加前缀了，错误用法： commit('sideBarTree/SET_ROOT_PID',root)
        commit('SET_ROOT_PID',root)
        //这里第二个参数是根节点的pid（注意不是根节点的id）
        Response.data.data = listToTree(Response.data.data, root )

        commit('SET_TREE', Response.data.data)
       

      })
    
  },

  

}

export default {
  namespaced:true,
  state,
  mutations,
  actions
}

