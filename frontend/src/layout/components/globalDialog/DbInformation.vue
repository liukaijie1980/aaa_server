<template>
    
    <div >
        <!-- <el-button type="primary" plain size="mini"  @click="showAddDialog" >数据库信息</el-button> -->
   
        <!--数据库信息的对话框-->
        <el-dialog title="数据库信息" :visible.sync="addDialogVisible" width="50%" @close="addDialogClosed" :append-to-body="true">
          <!--主体区域-->
          <el-card>
            <!--用户列表区域-->
            <el-table :data="userList" border stripe>
              <el-table-column label="文件名" prop="filename"></el-table-column>
              <el-table-column label="文件大小" prop="filesize"></el-table-column>
            </el-table>
            <!--分页区域-->
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
              :current-page="queryInfo.pagenum" :page-sizes="[1, 2, 5, 10]" :page-size="queryInfo.pagesize"
              layout="total, sizes, prev, pager, next, jumper" :total="total">
            </el-pagination>
          </el-card>
          <!--底部区域-->
          <span slot="footer" class="dialog-footer">
            <el-button @click="addDialogVisible = false">取 消</el-button>
            <el-button type="primary"  @click="addDialogVisible = false">确 定</el-button>
          </span>
        </el-dialog>   
      
    </div>
  </template>
  
<script>
  
  import { getDbInformation} from '@/api/globalDialog'

  export default {
   
  
    data() {
      return {
        // 获取用户列表的参数对象
        queryInfo: {
        query: '',
        // 当前的页数
        pagenum: 1,
        // 当前每页显示的条数
        pagesize: 10
      },
      userList: [],    
      total: 0,
      // 控制添加用户对话框显示与隐藏
      addDialogVisible: false,
      messages: [],
      }    
    },
    
    methods: {

      parentMsg: function (msg) {
      this.messages.push(msg);
      console.log("showAddDialog",msg);
      this.showAddDialog();
    },


        //获取用户列表
        getUserList() {

            getDbInformation(this.queryInfo.pagenum, this.queryInfo.pagesize)
                .then(
                    Response => {
                        console.log("after getDbInformation:Response.data.data", Response.data.data)
                        this.userList = Response.data.data.records;
                        this.total = Response.data.data.total;
                    })
                .catch(err => console.log("getDbInformation error:", err))

            console.log("after getDbInformation");
        },

        showAddDialog() {
           
            this.addDialogVisible = true;
            this.getUserList();

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

    // 监听添加用户对话框的关闭事件
    async addDialogClosed() {
      // 重置表单
     
    },

    }
  }
  </script>
  
  