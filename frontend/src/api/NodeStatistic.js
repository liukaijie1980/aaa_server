import request from '@/utils/request'


export function GetNodeStatistic( node_id) {  
    console.log("api GetNodeStatistic",node_id)
     
    return request({
      url: '/NodeStatistic',
      method: 'get',
      params:{
        node_id:node_id
      } 
    })
  }
