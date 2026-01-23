import request from '@/utils/request'
/* 本文件其实应该叫admin.js。但历史遗留原因所以暂时系统管理员在这里都叫user */


export function login(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}

export function getInfo(token) {
  return request({
    url: '/admin/info',
    method: 'get',
    params: { token }
  })
}

export function logout() {
  return request({
    url: '/admin/logout',
    method: 'post'
  })
}


export function GetAdministrators( Role,Nodeid,Name,PageNo,PageSize) {  
  console.log("api GetAdministrators:Nodeid",Nodeid)
  console.log("Role",Role)
  console.log("Name",Name)
  console.log("PageNo",PageNo)
  console.log("PageSize",PageSize)    
  return request({
    url: '/admin/administrator',
    method: 'get',
    params:{
      role:Role,
      nodeid:Nodeid,
      name:Name,
      pageNo:PageNo,
      pageSize:PageSize
    } 
  })
}





//request第3个参数data是固定不能改别的名字
export function AddAdministrator(data) {
  console.log("api AddAdministrator",data)
  return request({
    url: '/admin/administrator',
    method: 'post',
    data
  })
}

export function UpdateAdministrator(data) {
  console.log("api UpdateAdministrator",data)
  return request({
    url: '/admin/administrator',
    method: 'put',
    data
  })
}

export function DeleteAdministrator( UserName) {
  console.log("api DeleteAdministrator",UserName)
  return request({
    url: '/admin/administrator',
    method: 'delete',
    params:{name:UserName}
  })
}
