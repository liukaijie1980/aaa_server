const getters = {
  sidebar: state => state.app.sidebar,
  size: state => state.app.size,
  device: state => state.app.device,
  visitedViews: state => state.tagsView.visitedViews,
  cachedViews: state => state.tagsView.cachedViews,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  name: state => state.user.name,
  introduction: state => state.user.introduction,
  roles: state => state.user.roles,
  permission_routes: state => state.permission.routes,
  errorLogs: state => state.errorLog.logs,
  sidebarTree:state=>state.sideBarTree,
  nodeid:state=>state.user.nodeid,
  //因为已经用了计算属性，所以就不要用getters了
  // sidebarTree(state){
  // return  JSON.parse(JSON.stringify(state.sidebarTree.data))
  //},
  //data:state=>state.sideBarTree.data,
  rootPid:state=>state.sideBarTree.rootPid,
  treeObj:state=>state.sideBarTree.treeObj,
  currentNode: state=>state.sideBarTree.treeObj.getCurrentNode(),
}
export default getters
