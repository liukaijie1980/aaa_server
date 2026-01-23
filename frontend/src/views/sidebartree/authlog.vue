<template>
  <div>

    <el-card>
      <!--搜索与添加区域-->
      <el-row :gutter="20">
        <el-col :span="5">
          <el-input placeholder="请输入账号" v-model="queryInfo.username" clearable @clear="getUserList">
            <el-button slot="append" icon="el-icon-search" @click="getUserList"></el-button>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-select v-model="value" placeholder="请选择 域" @change="selectEventHandler" @remove-tag="selectEventHandler">
            <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-col>

        <el-date-picker v-model="queryInfo.DateFromTo" type="daterange" range-separator="至" start-placeholder="开始时间"
          end-placeholder="结束时间"  :default-value="timeDefaultShow"  :picker-options="pickerOptions"  @change="getUserList">
        </el-date-picker>
      </el-row>
     
    <el-row :gutter="20">        
        <el-col :span="2"    >               
            <el-tag type="info" >认证结果</el-tag>
        </el-col>
            <el-col :span="3"    >     
            <el-select v-model="queryInfo.reply"  @change="getUserList"  >
                <el-option v-for="item in OptionsForReply" :key="item.value" :label="item.label" :value="item.value" >
                </el-option>
            </el-select>               
        </el-col>    
        <el-col :span="5">
            <el-input placeholder="请输入用户mac" v-model="queryInfo.callingstationid" clearable @clear="getUserList">
                <el-button slot="append" icon="el-icon-search" @click="getUserList"></el-button>
            </el-input>
        </el-col>
        <el-col :span="5">
            <el-input placeholder="NAS Identifier" v-model="queryInfo.nasidentifier" clearable @clear="getUserList">
                <el-button slot="append" icon="el-icon-search" @click="getUserList"></el-button>
            </el-input>
        </el-col>
      </el-row>     
     

      <!--用户列表区域-->
      <el-table :data="userList" border stripe>  
        <el-table-column type="index" label="#"></el-table-column>
        <el-table-column label="账号" prop="username"></el-table-column>
        <el-table-column label="域" prop="realm"> </el-table-column>
        <el-table-column label="认证结果" prop="reply" ></el-table-column>        
        <el-table-column label="nas identifier" prop="nasidentifier"></el-table-column>        
        <el-table-column label="calling station id" prop="callingstationid"></el-table-column>    
        <el-table-column label="认证日期" prop="authdate"></el-table-column>    
      </el-table>

      <!--分页区域-->
      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
        :current-page="queryInfo.pagenum" :page-sizes="[1, 2, 5, 10]" :page-size="queryInfo.pagesize"
        layout="total, sizes, prev, pager, next, jumper" :total="total">
      </el-pagination>
    </el-card>


  </div>
</template>
    
<script>

import { Getradpostauth } from '@/api/history'
import { AddRealm, DeleteRealm, UpdateRealm, GetRealms } from '@/api/realm'

export default {
  props: ['NodeId'],


  data() {
    return {

      //域 选择框
      //可选项
      options: [{
        value: '',
        label: ''
      },],
      //选中的项
      value: [''],    

      //认证结果 选择框
      //可选项
      OptionsForReply: [
      {
        value: '',
        label: '全部'
      },{
        value: 'Access-Accept',
        label: '成功'
      },
      {
        value: 'Access-Reject',
        label: '失败'
      },
  ],


  //日期选择器
  timeDefaultShow:new Date(),
 
  daterange: '',
    pickerMinDate: '', // 第一次选中的时间
    pickerOptions: {
      onPick: obj => {
        console.log('obj', obj) // {maxDate: xxx, minDate: xxx(中国标准时间)}
        this.pickerMinDate = new Date(obj.minDate).getTime()
        if (obj.maxDate!=null)    {
           this.pickerMinDate='';
        }

      },
        disabledDate: time => {
            //限制只能选择明天以及7天之内的日期
            if (time.getTime() >  Date.now()+24*3600*1000 ||time.getTime() < Date.now()-7*24*3600*1000 ) {
               return true;
            }
            if (this.pickerMinDate) {
                const day1 = 7 * 24 * 3600 * 1000 // 可选的时间范围不超过7天
                let maxTime = this.pickerMinDate + day1
                let minTime = this.pickerMinDate - day1
                //console.log('return', time.getTime() > maxTime || time.getTime() < minTime) // 输出查看布尔值
                return time.getTime() > maxTime || time.getTime() < minTime
            }
      }
    },    
    

      // 获取用户列表的参数对象
      queryInfo: {
        username: '',
        reply:'',
        callingstationid:'',
        nasidentifier:'',
        DateFromTo:[],
        // 当前的页数
        pagenum: 1,
        // 当前每页显示的条数
        pagesize: 10
      },

      userList: [],
      total: 0,



    }
  },

  mounted() {
  //给日期控件赋初值
      this.timeDefaultShow = new Date();
      const today = new Date()
      const tomorrow = new Date(today)
      tomorrow.setDate(today.getDate() +1)         
      /*
      this.queryInfo.DateFromTo[0]=today;
      this.queryInfo.DateFromTo[1]=tomorrow;
      */      
      //上面这种形式无法直接显示在控件上，要用下面这种形式       
      this.queryInfo.DateFromTo=[today,tomorrow];
     
      
   

    new Promise((resolve, reject) => {
      console.log("mounted:getRealmList()")
      this.getRealmList(resolve, reject)

    }).then(() => { //上一个接口成功resolve之后执行then()
      console.log("mounted:getUserList()")
      this.getUserList()
    })


  },
  

  methods: {

    selectEventHandler() {
      //域被重新选择，要刷新账号数据
      this.getUserList()
    },

    //获取realm列表
    getRealmList(resolve, reject) {
      console.log("GetRealms: getCurrentNode", this.$store.getters.treeObj.getCurrentNode())
      GetRealms(this.$store.getters.treeObj.getCurrentNode().pid).then(
        Response => {
          console.log("after GetRealms:Response.data.data", Response.data.data)
          let res = [];
          res = Response.data.data;
          if (res.length > 0) {
            //给选择框设置默认值
            this.value = res[0].realm;
          }
          this.options.length = 0
          for (let i = 0; i < res.length; i++) {
            let obj = { value: '', label: '' }
            obj.value = res[i].realm;
            obj.label = res[i].realm;
            this.options.push(obj)
          }
          resolve()
        }).catch(err => {
          console.log("GetRealms error:", err)
          reject()
        })

      console.log("after GetRealms");
    },

 


    //获取用户列表
    getUserList() {
      console.log("this.value", this.value)//realm
      
 
      Getradpostauth(this.queryInfo.username.trim(), 
                     this.value,
                     this.queryInfo.reply,
                     this.queryInfo.callingstationid.trim(),
                     this.queryInfo.nasidentifier.trim(),
                     this.queryInfo.DateFromTo,
                     this.queryInfo.pagenum,
                     this.queryInfo.pagesize).then(
        Response => {
          console.log("after Getradpostauth:Response.data.data", Response.data.data)
          this.userList = Response.data.data.records;
          this.total = Response.data.data.total;

        }).catch(err => console.log("Getradpostauth error:", err))

      console.log("after Getradpostauth");
    },
    // 监听 pagesize 改变的事件
    handleSizeChange(newSize) {
      this.queryInfo.pagesize = newSize
      this.getUserList()
      console.log("handleSizeChange(newSize)", newSize)
    },
    // 监听页码值改变的事件
    handleCurrentChange(newPage) {
      this.queryInfo.pagenum = newPage
      this.getUserList()
      console.log(newPage)
    },




  }
}


</script>
    
<style lang="less" scoped>
  
  </style>
    
    
    
    
    
    
    
    
          
          
     