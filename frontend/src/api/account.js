import request from '@/utils/request'


export function GetAccountInfo( Name,Realm,PageNo,PageSize) {  
    console.log("api GetAccountInfo",Name+Realm)
    console.log("PageNo",PageNo)
    console.log("PageSize",PageSize)    
    return request({
      url: '/AccountInfo',
      method: 'get',
      params:{
          name:Name,
          realm:Realm,
          pageNo:PageNo,
          pageSize:PageSize
      } 
    })
  }



export function AddAccountInfo(data) {
  console.log("api AddAccountInfo",data)
  //request第3个参数data是固定不能改别的名字
  return request({
    url: '/AccountInfo',
    method: 'post',
    data
  })
}

export function UpdateAccountInfo(data) {
  console.log("api UpdateAccountInfo",data)
  //request第3个参数data是固定不能改别的名字
  return request({
    url: '/AccountInfo',
    method: 'put',
    data
  })
}

export function DeleteAccountInfo( name,realm) {
  console.log("api DeleteAccountInfo",name+realm)
  return request({
    url: '/AccountInfo',
    method: 'delete',
    params:{
        UserName:name,
        realm:realm,
    }
  })
}
