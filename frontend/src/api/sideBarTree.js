import request from '@/utils/request'



export function getTreeData(nodeid) {

  console.log("api->getTreedata", nodeid)
  return request(
    {
      url: '/SidebarTree',
      method: 'get',
      params: { nodeid }
    }
  )

}



export function setTreeData(data) {
  console.log("api->setTreedata:", data)
  return request({
    url: '/SidebarTree',
    method: 'post',
    data
  })
}

export function getNodeIdsByAcccount(username) {
  console.log("api->getNodeIdsByAcccount:", username)
  return request(
    {
      url: '/getNodeIdsByAcccount',
      method: 'get',
      params: { username }
    }
  )
}

export default { getTreeData, setTreeData, getNodeIdsByAcccount }