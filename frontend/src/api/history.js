import request from '@/utils/request'

export function GetOnlineUser(Name, Realm, PageNo, PageSize) {
  console.log("api GetOnlineUsser", Name + Realm)
  console.log("pageNo", PageNo)
  console.log("pageSize", PageSize)

  return request({
    url: '/OnlineUser',
    method: 'get',
    params: {
      name: Name,
      realm: Realm,
      pageNo: PageNo,
      pageSize: PageSize
    }
  })
}



export function UpdateOnlineUser(data) {
  console.log("api UpdateOnlineUser",data)
  //request第3个参数data是固定不能改别的名字
return request({
  url: '/OnlineUser',
  method: 'put',
  data
})

}







export function GetOfflineUser(Name, Realm, Framedipaddress, Callingstationid, DateFromTo, PageNo, PageSize) {
  console.log("api GetOfflineUsers", Name + Realm)
  console.log("framedipaddress", Framedipaddress)
  console.log("callingstationid", Callingstationid)
  let from = ''
  let to = ''
  if (DateFromTo) {
    from = DateFromTo[0]
    to = DateFromTo[1]
  }

  console.log("from", from)
  console.log("to", to)
  console.log("pageNo", PageNo)
  console.log("pageSize", PageSize)

  return request({
    url: '/OfflineUser',
    method: 'get',
    params: {
      name: Name,
      realm: Realm,
      framedipaddress: Framedipaddress,
      callingstationid: Callingstationid,
      from: from,
      to: to,
      pageNo: PageNo,
      pageSize: PageSize
    }
  })
}


export function Getradpostauth(Name, Realm, Reply, Callingstationid, NasIdentifier, DateFromTo, PageNo, PageSize) {
  console.log("api Getradpostauth", Name + Realm)
  console.log("reply", Reply)
  console.log("callingstationid", Callingstationid)
  console.log("nasidentifier", NasIdentifier)
  let from = ''
  let to = ''
  if (DateFromTo) {
    from = DateFromTo[0]
    to = DateFromTo[1]
  }

  console.log("from", from)
  console.log("to", to)
  console.log("pageNo", PageNo)
  console.log("pageSize", PageSize)

  return request({
    url: '/radpostauth',
    method: 'get',
    params: {
      name: Name,
      realm: Realm,
      reply: Reply,
      callingstationid: Callingstationid,
      nasidentifier: NasIdentifier,
      from: from,
      to: to,
      pageNo: PageNo,
      pageSize: PageSize
    }
  })
}



