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

        <el-date-picker v-model="queryInfo.DateFromTo" type="daterange" range-separator="至" start-placeholder="开始日期"
          end-placeholder="结束日期"  :default-value="timeDefaultShow"  :picker-options="pickerOptions" @change="getUserList">

        </el-date-picker>

      </el-row>
     
      <el-row :gutter="20">
        <el-col :span="5">
        
          <el-input placeholder="请输入用户ip" v-model="queryInfo.framedipaddress" clearable @clear="getUserList">
            <el-button slot="append" icon="el-icon-search" @click="getUserList"></el-button>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-input placeholder="请输入用户mac" v-model="queryInfo.callingstationid" clearable @clear="getUserList">
            <el-button slot="append" icon="el-icon-search" @click="getUserList"></el-button>
          </el-input>
        </el-col>
      </el-row>

     
      <!-- 筛选按钮 -->
      <el-popover placement="bottom" title="筛选列" trigger="click" width="40">
        <el-checkbox-group v-model="checkedColumns" size="mini">
          <el-checkbox v-for="item in checkBoxGroup" :key="item" :label="item" :value="item"></el-checkbox>
        </el-checkbox-group>
        <div title="筛选列" class="filter-table-col" slot="reference"><i class="el-icon-c-scale-to-original"></i></div>
      </el-popover>

      <!--用户列表区域-->
      <el-table :data="userList" border stripe>


        <el-table-column type="index" label="#"></el-table-column>
        <el-table-column label="账号" prop="username"></el-table-column>
        <el-table-column label="域" prop="realm"> </el-table-column>
        <el-table-column label="nas identifier" prop="nasidentifier" v-if="colData[0].istrue"></el-table-column>

        <el-table-column label="nas ip" prop="nasipaddress" v-if="colData[1].istrue"></el-table-column>
        <el-table-column label="nas portid" prop="nasportid" v-if="colData[2].istrue"></el-table-column>
        <el-table-column label="nas port type" prop="nasporttype" v-if="colData[3].istrue"></el-table-column>
        <el-table-column label="acct start time" prop="acctstarttime" v-if="colData[4].istrue"></el-table-column>
        <el-table-column label="acct update time" prop="acctupdatetime" v-if="colData[5].istrue"></el-table-column>
        <el-table-column label="acct stop time" prop="acctstoptime" v-if="colData[6].istrue"></el-table-column>

        <el-table-column label="acct interval" prop="acctinterval" v-if="colData[7].istrue"></el-table-column>
        <el-table-column label="acct session time" prop="acctsessiontime" v-if="colData[8].istrue"></el-table-column>
        <el-table-column label="acct authentic" prop="acctauthentic" v-if="colData[9].istrue"></el-table-column>
        <el-table-column label="connect info_start" prop="connectinfo_start"
          v-if="colData[10].istrue"></el-table-column>
        <el-table-column label="connect info_stop" prop="connectinfo_stop" v-if="colData[11].istrue"></el-table-column>
        <el-table-column label="acct input octets" prop="acctinputoctets" v-if="colData[12].istrue"></el-table-column>

        <el-table-column label="acct output octets" prop="acctoutputoctets" v-if="colData[13].istrue"></el-table-column>
        <el-table-column label="called station id" prop="calledstationid" v-if="colData[14].istrue"></el-table-column>
        <el-table-column label="calling station id" prop="callingstationid" v-if="colData[15].istrue"></el-table-column>
        <el-table-column label="acct terminate cause" prop="acctterminatecause" v-if="colData[16].istrue">
        </el-table-column>
        <el-table-column label="service type" prop="servicetype" v-if="colData[17].istrue"></el-table-column>
        <el-table-column label="framed protocol" prop="framedprotocol" v-if="colData[18].istrue"></el-table-column>

        <el-table-column label="framed ip address" prop="framedipaddress" v-if="colData[19].istrue"></el-table-column>
        <el-table-column label="framed ipv6 address" prop="framedipv6address"
          v-if="colData[20].istrue"></el-table-column>
        <el-table-column label="framed ipv6 prefix" prop="framedipv6prefix" v-if="colData[21].istrue"></el-table-column>
        <el-table-column label="framed interface id" prop="framedinterfaceid"
          v-if="colData[22].istrue"></el-table-column>
        <el-table-column label="delegated ipv6 prefix" prop="delegatedipv6prefix" v-if="colData[23].istrue">
        </el-table-column>

        <el-table-column label="_class" prop="_class" v-if="colData[24].istrue"></el-table-column>



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

import { GetOfflineUser } from '@/api/history'
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

      // colData中列出表格中的每一列，true就是默认展示，false就是默认隐藏
      colData: [
        { title: "nas identifier", istrue: true },
        { title: "nas ip", istrue: true },
        { title: "nas portid", istrue: false },
        { title: "nas port type", istrue: false },
        { title: "acct start time", istrue: true },
        { title: "acct update time", istrue: false },
        { title: "acct stop time", istrue: true },

        { title: "acct interval", istrue: false },
        { title: "acct session time", istrue: false },
        { title: "acct authentic", istrue: false },
        { title: "connect info_start", istrue: false },
        { title: "connect info_stop", istrue: false },
        { title: "acct input octets", istrue: true },

        { title: "acct output octets", istrue: true },
        { title: "called station id", istrue: false },
        { title: "calling station id", istrue: true },
        { title: "acct terminate cause", istrue: false },
        { title: "service type", istrue: false },
        { title: "framed protocol", istrue: false },

        { title: "framed ip address", istrue: true },
        { title: "framed ipv6 address", istrue: false },
        { title: "framed ipv6 prefix", istrue: false },
        { title: "framed interface id", istrue: false },
        { title: "delegated ipv6 prefix", istrue: false },
        { title: "_class", istrue: false },

      ],

      // 多选框的列表，列出表格的每一列     
      checkBoxGroup: ["nas identifier", "nas ip", "nas portid", "nas port type", "acct start time", "acct update time", "acct stop time",
        "acct interval", "acct session time", "acct authentic", "connect info_start", "connect info_stop", "acct input octets",
        "acct output octets", "called station id", "calling station id", "acct terminate cause", "service type", "framed protocol",
        "framed ip address", "framed ipv6 address", "framed ipv6 prefix", "framed interface id", "delegated ipv6 prefix", "_class"
      ],
      // 当前选中的多选框，代表当前展示的列      
      checkedColumns: ["nas identifier", "nas ip", "acct start time", "acct stop time", "acct input octets",
        "acct output octets", "calling station id", "framed ip address",],


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
            //限制只能选择明天以及一年之内的日期
            if (time.getTime() >  Date.now()+24*3600*1000 || time.getTime() <  Date.now()-365*24*3600*1000  ) {
               return true;
            }
            if (this.pickerMinDate) {
                const day1 = 31 * 24 * 3600 * 1000 // 可选的时间范围不超过31天
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
        framedipaddress:'',
        callingstationid:'',
        DateFromTo:'',
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
  //筛选显示的列
  watch: {
    checkedColumns(val) {
      let arr = this.checkBoxGroup.filter(i => !val.includes(i));
      this.colData.filter(i => {
        if (arr.indexOf(i.title) != -1) {
          i.istrue = false;
        } else {
          i.istrue = true;
        }
      });
      this.reload = Math.random()
    }
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
      
 
      GetOfflineUser(this.queryInfo.username.trim(), 
                     this.value,
                     this.queryInfo.framedipaddress.trim(),
                     this.queryInfo.callingstationid.trim(),
                     this.queryInfo.DateFromTo,
                     this.queryInfo.pagenum,
                     this.queryInfo.pagesize).then(
        Response => {
          console.log("after GetOfflineUser:Response.data.data", Response.data.data)
          this.userList = Response.data.data.records;
          this.total = Response.data.data.total;

        }).catch(err => console.log("GetOfflineUser error:", err))

      console.log("after GetOfflineUser");
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
  
  
  
  
  
  
  
  
        
        
   