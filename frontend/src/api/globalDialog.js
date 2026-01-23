import request from '@/utils/request'

export function getDbInformation(PageNo, PageSize) {
  console.log('api getDbInformation')
  console.log('pageNo', PageNo)
  console.log('pageSize', PageSize)

  return request({
    url: '/DbInformation',
    method: 'get',
    params: {
      pageNo: PageNo,
      pageSize: PageSize
    }
  })
}

// ---------------------------------------------------nas
export function GetNas() {
  console.log('api GetNas')

  return request({
    url: '/nas',
    method: 'get'

  })
}

export function AddNas(data) {
  console.log('api AddNas', data)
  // request第3个参数data是固定不能改别的名字
  return request({
    url: '/nas',
    method: 'post',
    data
  })
}

export function UpdateNas(data) {
  console.log('api UpdateNas', data)
  // request第3个参数data是固定不能改别的名字
  return request({
    url: '/nas',
    method: 'put',
    data
  })
}

export function DeleteNas(id) {
  console.log('api DeleteNas', id)
  return request({
    url: '/nas',
    method: 'delete',
    params: {

      id: id
    }
  })
}

export function ApplyNas() {
  console.log('api ApplyNas')

  return request({
    url: '/docker/restart',
    method: 'post'

  })
}

export function getGitVersion() {
  console.log('api getGitVersion');
  axios.get(process.env.VUE_APP_BASE_API + '/actuator/info', {
    headers: {
      'x-token': 'your-token-value'
    }
  })
    .then(response => {
      this.info = response.data;
    })
    .catch(error => {
      this.error = error;
    });
}


