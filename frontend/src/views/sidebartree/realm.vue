<template>   

    <div>

        <el-card>
            <!-- <el-tag type="info">父节点id:     {{ParentId}}</el-tag> -->
            <el-tag type="info">以下是本节点管理的域,推荐一个节点有且只有一个域</el-tag>
            <el-row :gutter="20">                 
                
                <el-col :span="4">
                    <el-button type="primary" @click="addDialogVisible = true">添加域(realm)</el-button>
                </el-col>
            </el-row>

            <!--用户列表区域-->
            <el-table :data="userList" border stripe>
                <el-table-column type="index" label="#"></el-table-column>

                <el-table-column label="域" prop="realm"></el-table-column>

                <el-table-column label="操作" width="180px">
                    <template slot-scope="scope">

                        <!-- 删除按钮 -->
                        
                            <el-tooltip effect="dark" content="删除" placement="top-start" :enterable="false">
                                <el-button type="danger" icon="el-icon-delete" size="mini"
                                    @click="removeUserById(scope.$index)">
                                </el-button>
                            </el-tooltip>
                       

                    </template>
                </el-table-column>
            </el-table>
        </el-card>
        <!--添加用户的对话框-->
        <el-dialog title="添加域" :visible.sync="addDialogVisible" width="50%" @close="addDialogClosed">
            <!--主体区域-->
            <el-form :model="addForm" :rules="FormRules" ref="addFormRef" label-width="200px">
                <el-form-item label="域" prop="realm">
                    <el-input v-model="addForm.realm"></el-input>
                </el-form-item>
            </el-form>

            <!--底部区域-->
            <span slot="footer" class="dialog-footer">
                <el-button @click="addDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="addUser">确 定</el-button>
            </span>
        </el-dialog>



    </div>
</template>
  
<script>

import { AddRealm, DeleteRealm, UpdateRealm, GetRealms } from '@/api/realm'


export default {
    props: ['NodeId'],


    data() {

               // 校验realm
               var checkRealm = (rule, value, cb) => {
            const regEmail =/@+[a-zA-Z0-9-]/
            if (regEmail.test(value)) {
                // 合法realm
                return cb()
            }
            cb(new Error('域名必须带 "@" "'))
        }

        return {
           
            // ParentId: this.$store.getters.currentNode.pid,
            

            userList: [],
            total: 0,
            // 控制添加用户对话框显示与隐藏
            addDialogVisible: false,
            // 添加用户的表单数据
            addForm: {
                realm: '',
                node_id: this.$store.getters.currentNode.id
            },
      
           
 // 添加表单验证规则对象
 FormRules: {
        realm: [
       
          {
            required: true,
            message: '请输入域名',
            trigger: 'blur'
          },
          {
            min: 1,
            max: 40,
            message: '域名的长度在1-40个字符之间,并且必须以\“@\”开头  ',
            validator: checkRealm,
            trigger: 'blur'
          }
       
          
        ],    
       
      },

            

        }
    },
    created() {
        this.getUserList()
    },
    methods: {

        //获取用户列表
        getUserList() {
            console.log("GetRealms: getCurrentNode", this.$store.getters.treeObj.getCurrentNode() )
            GetRealms(this.$store.getters.treeObj.getCurrentNode().id).then(
                Response => {
                    console.log("after GetRealms:Response.data.data", Response.data.data)
                    this.userList = Response.data.data;
                    
                }).catch(err => console.log("GetRealms error:", err))

            console.log("after GetRealms");
        },




        // 监听添加用户对话框的关闭事件
        async addDialogClosed() {
            // 重置表单
            this.$refs.addFormRef.resetFields()
        },
        // 点击按钮，添加新用户
        addUser() {
            this.$refs.addFormRef.validate(async valid => {
                if (!valid) return
                // 可以发起添加用户的网络请求

                
                console.log("AddRealm: this.addForm", this.addForm)

                AddRealm(this.addForm)
                    .then(Response => {
                        console.log("AddRealm finish")
                        // 添加成功
                        this.$message.success("添加成功")
                        // 隐藏弹出框
                        this.addDialogVisible = false
                        // 刷新用户列表
                        this.getUserList()
                    }).catch(err => {
                        console.log(err);
                    })

            })

            console.log("after AddRealm");
        },


        // 根据id删除对应的用户信息
        async removeUserById(id) {
            // 弹框提示用户是否删除
            const confirmResult = await this.$confirm(
                '此操作将删除该域, 是否继续?',
                '提示',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }
            ).catch(err => err)
            // 如果用户确认删除，则返回字符串 confirm
            // 如果用户取消删除，则返回字符串 cancel
            if (confirmResult !== 'confirm') {
                return this.$message.info('已取消删除')
            }

            DeleteRealm(this.userList[id].realm)
                .then(Response => {
                    console.log("DeleteRealm finish")
                    // 更新成功
                    this.$message.success('删除成功')

                    // 刷新用户列表
                    this.getUserList()

                }).catch(err => {
                    console.log(err);
                })
            console.log("after DeleteRealm");

        }
    }
}


</script>
  
<style lang="less" scoped>

</style>
  
  
  
  
  
  
  
  
        
        
   