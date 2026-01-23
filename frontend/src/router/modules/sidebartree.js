/** When your routing table is too long, you can split it into small modules **/

import Layout from '@/layout'
const sidebartreeRouter={

    path: '/sidebartree',
    component: Layout,
   

    children: [
    
      {
        path: 'leaf/:NodeId',
        component: () => import('@/views/sidebartree/leaf.vue'),
        name: 'leaf',    
        props:true,     
        meta: {
        title: '叶子节点', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      },
      {
        path: 'folder/:NodeId',
        component: () => import('@/views/sidebartree/folder.vue'),
        name: 'folder',    
        props:true,     
        meta: {
        title: '文件夹节点', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      },
      {
        path: 'realm/:NodeId',
        component: () => import('@/views/sidebartree/realm.vue'),
        name: 'realm',    
        props:true,     
        meta: {
        title: '管理域', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      },
      {
        path: 'account/:NodeId',
        component: () => import('@/views/sidebartree/account.vue'),
        name: 'account',    
        props:true,     
        meta: {
        title: '上网账号', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      },
      {
        path: 'offlineuser/:NodeId',
        component: () => import('@/views/sidebartree/offlineuser.vue'),
        name: 'offlineuser',    
        props:true,     
        meta: {
        title: '上网记录', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      },
      {
        path: 'messages/:NodeId',
        component: () => import('@/views/sidebartree/messages.vue'),
        name: 'messages',    
        props:true,     
        meta: {
        title: '短信群发', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      },
      {
        path: 'webportal/:NodeId',
        component: () => import('@/views/sidebartree/webportal.vue'),
        name: 'webportal',    
        props:true,     
        meta: {
        title: '页面设置', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      },
      {
        path: 'onlineuser/:NodeId',
        component: () => import('@/views/sidebartree/onlineuser.vue'),
        name: 'onlineuser',    
        props:true,     
        meta: {
        title: '在线用户', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      }
      ,
      {
        path: 'authlog/:NodeId',
        component: () => import('@/views/sidebartree/authlog.vue'),
        name: 'authlog',    
        props:true,     
        meta: {
        title: '认证日志', icon: 'icon', noCache: true , 
        roles: ['admin','editor']
       }
      }

    ]
    
    

    
  }

  export default sidebartreeRouter