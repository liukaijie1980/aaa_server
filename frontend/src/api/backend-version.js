
import axios from 'axios'
import request from '@/utils/request'

import { getToken } from '@/utils/auth'

//因为访问的后端这个api的返回值是第3方，格式和其他api不同,所以不能用request方法
//这个api已经被弃用，请使用getGit()
export function getGitVersion() {
    console.log('api getGitVersion');

    const url = process.env.VUE_APP_BASE_API + '/actuator/info';
    const headers = {};

    if (getToken()) {
        headers['x-token'] = getToken();
    }

    return  axios.get(url, { headers: headers })
        .then(response => {
           console.log("response.data",response.data);
           return response.data.git.commit.id;               
        })
        .catch(error => {
            console.error("Error fetching data:", error);
        });
}

export function getGit( ) {  
    console.log("api getGit")
     
    return request({
      url: '/actuator-info',
      method: 'get',
      
    })
  }
