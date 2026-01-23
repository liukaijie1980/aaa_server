<template>


    <div>
        <!-- 面包屑导航区域-->
         <!-- <el-breadcrumb separator-class="el-icon-arrow-right">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>用户管理</el-breadcrumb-item>
        <el-breadcrumb-item>用户列表</el-breadcrumb-item>
      </el-breadcrumb> -->
        <!--卡片试图区域-->
        <el-card>
            <!--搜索与添加区域-->
            <el-row :gutter="20">
                <el-col :span="15">
                    <el-input placeholder="请输入姓名" v-model="queryInfo.query" clearable @clear="getUserList">
                        <el-button slot="append" icon="el-icon-search" @click="getUserList"></el-button>
                    </el-input>
                </el-col>
                <el-col :span="4">
                    <el-button type="primary" @click="addDialogVisible = true">添加操作员</el-button>
                </el-col>
            </el-row>

            <!--用户列表区域-->
            <el-table :data="userList" border stripe>
                <el-table-column type="index" label="#"></el-table-column>
                <el-table-column label="姓名" prop="name"></el-table-column>
                <el-table-column label="邮箱" prop="email"></el-table-column>
                <el-table-column label="电话" prop="phone"></el-table-column>
                <el-table-column label="角色" prop="role"></el-table-column>
              <!--   <el-table-column label="状态">
                    <template slot-scope="scope">
                        <el-switch v-model="scope.row.mg_state" @change="userStateChanged(scope.$index)"></el-switch>
                    </template>
                </el-table-column> -->
                <el-table-column label="操作" width="180px">
                    <template slot-scope="scope">
                        <!-- 修改按钮 -->
                        <el-tooltip effect="dark" content="修改" placement="top-start" :enterable="false">
                        <el-button type="primary" icon="el-icon-edit" size="mini" @click="showEditDialog(scope.$index)">
                        </el-button>
                        </el-tooltip>

                        <!-- 删除按钮 -->
                         <!-- 不能删除admin这个系统管理员 -->
                        <span v-if="scope.row.name!=='admin'" >
                        <el-tooltip effect="dark" content="删除" placement="top-start" :enterable="false">
                        <el-button type="danger" icon="el-icon-delete" size="mini"
                            @click="removeUserById(scope.$index)"></el-button>
                        </el-tooltip>
                        </span>
                        <!--   <el-tooltip effect="dark" content="分配角色" placement="top-start" :enterable="false">
                            <el-button type="warning" icon="el-icon-setting" size="mini"></el-button>
                        </el-tooltip> -->
                    </template>
                </el-table-column>
            </el-table>

            <!--分页区域-->
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="queryInfo.pagenum" :page-sizes="[1, 2, 5, 10]" :page-size="queryInfo.pagesize"
                layout="total, sizes, prev, pager, next, jumper" :total="total">
            </el-pagination>
        </el-card>

        <!--添加用户的对话框-->
        <el-dialog title="添加用户" :visible.sync="addDialogVisible" width="50%" @close="addDialogClosed">
            <!--主体区域-->
            <el-form :model="addForm" :rules="addFormRules" ref="addFormRef" label-width="70px">
                <el-form-item label="用户名" prop="name">
                    <el-input v-model="addForm.name"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="addForm.password" show-password></el-input>
                </el-form-item>

                <el-form-item label="角色" prop="role">
                    <el-radio-group v-model="addForm.role">
                        <!-- 注意这里label 不要加: -->
                        <el-radio  label="admin"></el-radio>
                        <el-radio  label="editor"></el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="addForm.email"></el-input>
                </el-form-item>
                <el-form-item label="手机" prop="phone">
                    <el-input v-model="addForm.phone"></el-input>
                </el-form-item>
            </el-form>
            <!--底部区域-->
            <span slot="footer" class="dialog-footer">
                <el-button @click="addDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="addUser">确 定</el-button>
            </span>
        </el-dialog>
        <!--修改用户对话框-->
        <el-dialog title="修改用户" :visible.sync="editDialogVisible" width="50%" @close="editDialogClosed">
            <el-form :model="editForm" :rules="editFormRules" ref="editFormRef" label-width="70px">
                <el-form-item label="用户名">
                    <el-input v-model="editForm.name" disabled></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="editForm.password"  show-password></el-input>
                </el-form-item>
                <el-form-item label="角色" prop="role">
                    <el-radio-group v-model="editForm.role">
                        <el-radio label="admin"></el-radio>
                        <el-radio label="editor"></el-radio>
                    </el-radio-group>
                </el-form-item>              

                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="editForm.email"></el-input>
                </el-form-item>
                <el-form-item label="手机" prop="phone">
                    <el-input v-model="editForm.phone"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="editDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="editUserInfo">确 定</el-button>
            </span>
        </el-dialog>
    </div>
</template>
  
<script>


import { DeleteAdministrator, UpdateAdministrator, AddAdministrator, GetAdministrators } from '@/api/user'

export default {
    props: ['NodeId'],
    data() {


        // 校验邮箱
        var checkEmail = (rule, value, cb) => {
            const regEmail = /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/
            if (regEmail.test(value)) {
                // 合法邮箱
                return cb()
            }
            cb(new Error('请输入合法的邮箱'))
        }
        // 校验手机号
        var checkMobile = (rule, value, cb) => {
            const regMobile = /^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/
            if (regMobile.test(value)) {
                return cb()
            }
            cb(new Error('请输入合法手机号'))
        }
        return {
           
            // 获取用户列表的参数对象
            queryInfo: {
                query: '',
                // 当前的页数
                pagenum: 1,
                // 当前每页显示的条数
                pagesize: 5
            },
            userList: [],
            total: 0,
            // 控制添加用户对话框显示与隐藏
            addDialogVisible: false,
            // 添加用户的表单数据
            addForm: {
                name: '',
                password: '',
                email: '',
                phone: '',
                nodeid: this.$store.getters.treeObj.getCurrentNode() .id,
                       
                role: ''
            },
            // 添加表单验证规则对象
            addFormRules: {
                name: [
                    {
                        required: true,
                        message: '请输入用户名',
                        trigger: 'blur'
                    },
                    {
                        min: 6,
                        max: 10,
                        message: '用户名的长度在6-10个字符之间',
                        trigger: 'blur'
                    }
                ],
                password: [
                    {
                        required: true,
                        message: '请输入密码',
                        trigger: 'blur'
                    },
                    {
                        min: 6,
                        max: 15,
                        message: '密码的长度在6-15个字符之间',
                        trigger: 'blur'
                    }
                ],
                role: [
                    {
                        required: true,
                        message: '请选择角色',
                        trigger: 'blur'
                    },

                ],
                email: [
                    {
                        required: true,
                        message: '请输入邮箱',
                        trigger: 'blur'
                    },
                    {
                        validator: checkEmail,
                        trigger: 'blur'
                    }
                ],
                phone: [
                    {
                        required: true,
                        message: '请输入手机',
                        trigger: 'blur'
                    },
                    {
                        validator: checkMobile,
                        trigger: 'blur'
                    }
                ]
            },
            // 修改用户对话框
            editDialogVisible: false,
            editForm: {
                name: '',
                password: '',
                email: '',
                phone: '',
                nodeid: this.$store.getters.treeObj.getCurrentNode() .id,
                role: ''

            },
            // 修改表单验证对象
            editFormRules: {
                password: [
                    {
                        required: true,
                        message: '请输入密码',
                        trigger: 'blur'
                    },
                    {
                        min: 6,
                        max: 15,
                        message: '密码的长度在6-15个字符之间',
                        trigger: 'blur'
                    }
                ],
                role: [
                    {
                        required: true,
                        message: '请选择角色',
                        trigger: 'blur'
                    }
                ],
                email: [
                    {
                        required: true,
                        message: '请输入邮箱',
                        trigger: 'blur'
                    },
                    {
                        validator: checkEmail,
                        trigger: 'blur'
                    }
                ],
                phone: [
                    {
                        required: true,
                        message: '请输入手机',
                        trigger: 'blur'
                    },
                    {
                        validator: checkMobile,
                        trigger: 'blur'
                    }
                ]
            }
        }
    },
    created() {
        this.getUserList()
    },
    methods: {
        //用户列表
        getUserList() {              
            GetAdministrators(
                          this.$store.getters.roles, 
                          this.$store.getters.treeObj.getCurrentNode() .id,
                          this.queryInfo.query,
                          this.queryInfo.pagenum, 
                          this.queryInfo.pagesize 
            ).then(
                Response => {
                    this.userList = Response.data.data.records;
                    this.total = Response.data.data.total;

                }).catch(err => console.log("GetAdministrators error:", err))

            console.log("after GetAdministrators");
        },
        // 监听 pagesize 改变的事件
        handleSizeChange(newSize) {
            this.queryInfo.pagesize = newSize
            this.getUserList()
            console.log("handleSizeChange(newSize)",newSize)
        },
        // 监听页码值改变的事件
        handleCurrentChange(newPage) {
            this.queryInfo.pagenum = newPage
            this.getUserList()
            console.log(newPage)
        },
        // 修改用户状态
        async userStateChanged(userInfo) {
            const { data: res } = await this.$http.put(
                `users/${userInfo.id}/state/${userInfo.mg_state}`
            )
            if (res.meta.status !== 200) {
                userInfo.mg_state = !userInfo.mg_state
                return this.$message.error(res.meta.msg)
            }
            this.$message.success(res.meta.msg)
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
                /* const { data: res } = await this.$http.post('users', this.addForm)
                if (res.meta.status !== 201) return this.$message.error(res.meta.msg) */
                console.log("add_administrator: getCurrentNode", this.$store.getters.treeObj.getCurrentNode() )

                console.log("add_administrator: this.addForm", this.addForm)
                AddAdministrator(this.addForm)
                    .then(Response => {
                        console.log("AddAdministrator finish")
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

            console.log("after AddAdministrator");
        },

        // 修改用户
        async showEditDialog(id) {

            console.log("showEditDialog:id", id);
            console.log("showEditDialog:this.userList[id]", this.userList[id]);
            this.editForm = this.userList[id];

            this.editDialogVisible = true




        },
        // 监听修改用户对话框的关闭事件
        editDialogClosed() {
            this.$refs.editFormRef.resetFields()
        },
        // 修改用户信息并提交
        editUserInfo() {
            this.$refs.editFormRef.validate(async valid => {
                if (!valid) return                
                // 发起修改用户信息的数据请求
                UpdateAdministrator(this.editForm)
                    .then(Response => {
                        console.log("UpdateAdministrator finish")
                        // 更新成功
                        this.$message.success('更新成功')
                        // 刷新用户列表
                        this.getUserList()
                        // 关闭对话框
                        this.editDialogVisible = false
                    }).catch(err => {
                        console.log(err);
                    })
                console.log("after UpdateAdministrator");


            })
        },
        // 根据id删除对应的用户信息
        async removeUserById(id) {
            // 弹框提示用户是否删除
            const confirmResult = await this.$confirm(
                '此操作将永久删除该用户, 是否继续?',
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
           
            DeleteAdministrator(this.userList[id].name)
                    .then(Response => {
                        console.log("DeleteAdministrator finish")
                        // 更新成功
                        this.$message.success('删除成功')
                        
                        // 刷新用户列表
                        this.getUserList()
                       
                    }).catch(err => {
                        console.log(err);
                    })
                console.log("after DeleteAdministrator");            
           
        }
    }
}


</script>
  
<style lang="less" scoped>

</style>
  
  
  
  
  
  
  
  
        
        
   