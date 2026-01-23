<template>
  <div>
    <!-- <el-button type="primary" plain size="mini" @click="showNasDialog">Nas信息</el-button> -->

    <!--数据库信息的对话框-->
    <el-dialog title="NAS客户端" :visible.sync="NasDialogVisible" width="70%" :append-to-body="true">
      <!--主体区域-->
      <el-card>
        <el-button type="primary" @click="addDialogVisible = true">添加客户端(Nas)</el-button>

        <!--用户列表区域-->
        <el-table :data="userList" border stripe>
          <el-table-column label="操作" width="180px">
            <template slot-scope="scope">
              <!-- 修改按钮 -->
              <el-tooltip effect="dark" content="修改" placement="top-start" :enterable="false">
                <el-button
                  type="primary"
                  icon="el-icon-edit"
                  size="mini"
                  @click="showEditDialog(scope.$index)"
                />
              </el-tooltip>
              <!-- 删除按钮 -->
              <el-tooltip effect="dark" content="删除" placement="top-start" :enterable="false">
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  @click="removeUserById(scope.$index)"
                />
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="id" prop="id" />
          <el-table-column label="NAS IP/mask" prop="nasname" />
          <el-table-column label="shortname" prop="shortname" />
          <el-table-column label="type" prop="type" />
          <el-table-column label="ports" prop="ports" />
          <el-table-column label="共享密钥" prop="secret" />
          <el-table-column label="coa密钥" prop="coa" />
          <el-table-column label="上下行反转" prop="reversal" :formatter="reversalFormat" />
          <el-table-column label="server" prop="server" />
          <el-table-column label="community" prop="community" />
          <el-table-column label="description" prop="description" />
        </el-table>

      </el-card>
      <!--底部区域-->
      <span slot="footer" class="dialog-footer">
        <el-button @click="NasDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="applyNasConfig">重启并应用</el-button>
      </span>
    </el-dialog>

    <div>
      <!--修改Nas的对话框-->
      <el-dialog
        title="修改Nas"
        :visible.sync="editDialogVisible"
        width="70%"
        :append-to-body="true"
        @close="editDialogClosed"
      >

        <!--主体区域-->
        <el-form ref="editFormRef" :model="editForm" label-width="200px" :rules="editFormRules">
          <el-form-item label="NAS IP/mask" prop="nasname">
            <el-input v-model="editForm.nasname" />
          </el-form-item>
          <el-form-item label="shortname" prop="shortname">
            <el-input v-model="editForm.shortname" />
          </el-form-item>
          <el-form-item label="type" prop="type">
            <el-input v-model="editForm.type" />
          </el-form-item>
          <el-form-item label="ports" prop="ports">
            <el-input v-model="editForm.ports" />
          </el-form-item>
          <el-form-item label="共享密钥" prop="secret">
            <el-input v-model="editForm.secret" />
          </el-form-item>
          <el-form-item label="coa密钥" prop="coa">
            <el-input v-model="editForm.coa" />
          </el-form-item>

          <el-form-item label="上下行反转" prop="reversal">
            <el-radio-group v-model="editForm.reversal">
              <el-radio :label="true">是</el-radio>
              <el-radio :label="false">否</el-radio>
            </el-radio-group>

          </el-form-item>

          <el-form-item label="server" prop="server">
            <el-input v-model="editForm.server" />
          </el-form-item>
          <el-form-item label="community" prop="community">
            <el-input v-model="editForm.community" />
          </el-form-item>
          <el-form-item label="description" prop="description">
            <el-input v-model="editForm.description" />
          </el-form-item>
        </el-form>

        <!--底部区域-->
        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="editUser">确 定</el-button>
        </span>
      </el-dialog>

      <!--添加Nas的对话框-->
      <el-dialog
        title="添加Nas"
        :visible.sync="addDialogVisible"
        width="50%"
        :append-to-body="true"
        @close="addDialogClosed"
      >

        <!--主体区域-->
        <el-form ref="addFormRef" :model="addForm" label-width="200px" :rules="addFormRules">
          <el-form-item label="NAS IP/mask" prop="nasname">
            <el-input v-model="addForm.nasname" />
          </el-form-item>
          <el-form-item label="shortname" prop="shortname">
            <el-input v-model="addForm.shortname" />
          </el-form-item>
          <el-form-item label="type" prop="type">
            <el-input v-model="addForm.type" />
          </el-form-item>
          <el-form-item label="ports" prop="ports">
            <el-input v-model="addForm.ports" />
          </el-form-item>
          <el-form-item label="共享密钥" prop="secret">
            <el-input v-model="addForm.secret" />
          </el-form-item>
          <el-form-item label="coa密钥" prop="coa">
            <el-input v-model="addForm.coa" />
          </el-form-item>

          <el-form-item label="上下行反转" prop="reversal">
            <el-radio-group v-model="addForm.reversal">
              <el-radio :label="true">是</el-radio>
              <el-radio :label="false">否</el-radio>
            </el-radio-group>

          </el-form-item>

          <el-form-item label="server" prop="server">
            <el-input v-model="addForm.server" />
          </el-form-item>
          <el-form-item label="community" prop="community">
            <el-input v-model="addForm.community" />
          </el-form-item>
          <el-form-item label="description" prop="description">
            <el-input v-model="addForm.description" />
          </el-form-item>
        </el-form>

        <!--底部区域-->
        <span slot="footer" class="dialog-footer">
          <el-button @click="addDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addUser">确 定</el-button>
        </span>
      </el-dialog>

    </div>

  </div>
</template>

<script>

import { AddNas, DeleteNas, UpdateNas, GetNas, ApplyNas } from '@/api/globalDialog'
import { Divider } from 'element-ui'

export default {
  components: { Divider },
  data() {
    // 检查ports是否纯数字
    var checkports = (rule, value, cb) => {
      const regMobile = /^[0-9]*$/
      if (regMobile.test(value)) {
        return cb()
      }
      cb(new Error('请输入纯数字'))
    }
    return {
      messages: [],
      userList: [],
      // 控制添加用户对话框显示与隐藏
      addDialogVisible: false,
      editDialogVisible: false,
      NasDialogVisible: false,
      editForm: {
        nasname: '*',
        shortname: 'any',
        type: 'other',
        ports: '0',
        secret: '',
        coa: '',
        reversal: false,
        server: '',
        community: '',
        description: 'radius client'
      },

      addForm: {
        nasname: '*',
        shortname: 'any',
        type: 'other',
        ports: '0',
        secret: '',
        coa: '',
        reversal: false,
        server: '',
        community: '',
        description: 'radius client'
      },
      // 添加表单验证规则对象
      addFormRules: {
        nasname: [
          {
            min: 0,
            max: 64,
            message: 'Nas名的长度在0-64个字符之间',
            trigger: 'blur'
          }
        ],
        shortname: [
          {
            min: 0,
            max: 16,
            message: '缩略名的长度在0-16个字符之间',
            trigger: 'blur'
          }
        ],
        type: [
          {
            min: 0,
            max: 15,
            message: 'Nas类型的长度在0-15个字符之间',
            trigger: 'blur'
          }
        ],
        ports: [
          {
            validator: checkports,
            trigger: 'blur'
          }
        ],
        secret: [
          {
            required: true,
            message: '请输入Nas共享密钥',
            trigger: 'blur'
          },
          {
            min: 1,
            max: 30,
            message: 'Nas共享密钥的长度在1-30个字符之间',
            trigger: 'blur'
          }
        ],
        coa: [
          {
            required: true,
            message: '请输入coa报文共享密钥',
            trigger: 'blur'
          },
          {
            min: 1,
            max: 30,
            message: 'coa共享密钥的长度在1-30个字符之间',
            trigger: 'blur'
          }
        ],
        community: [
          {
            min: 0,
            max: 25,
            message: '社团名的长度在0-25个字符之间',
            trigger: 'blur'
          }
        ],
        description: [
          {
            min: 0,
            max: 100,
            message: '描述的长度在0-100个字符之间',
            trigger: 'blur'
          }
        ]
      },
      // 不重复定义，初始化时拷贝addFormRules对象
      editFormRules: null
    }
  },
  created() {
    // 拷贝addFormRules对象到editFormRules
    this.editFormRules = { ...this.addFormRules }
  },

  methods: {
    reversalFormat(row, column) {
      if (row.reversal) {
        return '是'
      } else {
        return '否'
      }
    },
    parentMsg: function(msg) {
      console.log('showNasDialog', msg)
      this.messages.push(msg)
      this.showNasDialog()
    },
    // 获取用户列表
    getUserList() {
      GetNas().then(Response => {
        console.log('after GetNas:Response.data.data', Response.data.data)
        this.userList = Response.data.data
      }).catch(err => console.log('GetNas error:', err))
      console.log('after GetNas')
    },
    showNasDialog() {
      this.NasDialogVisible = true
      this.getUserList()
    },
    // 监听添加用户对话框的关闭事件
    async addDialogClosed() {
      // 重置表单
      this.$refs.addFormRef.resetFields()
    },
    // 点击按钮，添加新用户
    addUser() {
      this.$refs.addFormRef.validate(async(valid) => {
        if (!valid) { return }
        // 可以发起添加用户的网络请求
        console.log('AddNas: this.addForm', this.addForm)
        AddNas(this.addForm)
          .then(Response => {
            console.log('AddNas finish')
            // 添加成功
            this.$message.success('添加成功')
            // 隐藏弹出框
            this.addDialogVisible = false
            // 刷新用户列表
            this.getUserList()
          }).catch(err => {
            console.log(err)
          })
      })
      console.log('after AddNas')
    },
    // 根据id删除对应的用户信息
    async removeUserById(id) {
      // 弹框提示用户是否删除
      const confirmResult = await this.$confirm('此操作将删除该域, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).catch(err => err)
      // 如果用户确认删除，则返回字符串 confirm
      // 如果用户取消删除，则返回字符串 cancel
      if (confirmResult !== 'confirm') {
        return this.$message.info('已取消删除')
      }
      DeleteNas(this.userList[id].id)
        .then(Response => {
          console.log('DeleteNas finish')
          // 更新成功
          this.$message.success('删除成功')
          // 刷新用户列表
          this.getUserList()
        }).catch(err => {
          console.log(err)
        })
      console.log('after DeleteNas')
    },

    // 点击按钮，添加新用户
    editUser() {
      this.$refs.editFormRef.validate(async(valid) => {
        if (!valid) { return }
        // 可以发起添加用户的网络请求
        console.log('editNas: this.editForm', this.editForm)
        UpdateNas(this.editForm)
          .then(Response => {
            console.log('editNas finish')
            // 添加成功
            this.$message.success('修改成功')
            // 隐藏弹出框
            this.addDialogVisible = false
            // 刷新用户列表
            this.getUserList()
          }).catch(err => {
            console.log(err)
          })
      })
      console.log('after editNas')
    },

    // 监听添加用户对话框的关闭事件
    async editDialogClosed() {
      // 重置表单
      this.$refs.editFormRef.resetFields()
    },
    // 修改用户
    async showEditDialog(id) {
      console.log('showEditDialog:id', id)
      console.log('showEditDialog:this.userList[id]', this.userList[id])
      this.editForm = this.userList[id]
      this.editDialogVisible = true
    },

    // 根据id删除对应的用户信息
    async  applyNasConfig() {
      // 弹框提示用户是否删除
      const confirmResult = await this.$confirm('此操作将重启radius服务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).catch(err => err)
      // 如果用户确认重启，则返回字符串 confirm
      // 如果用户取消重启，则返回字符串 cancel
      if (confirmResult !== 'confirm') {
        return this.$message.info('已取消重启')
      }
      ApplyNas()
        .then(Response => {
          console.log('ApplyNas finish')
          // 更新成功
          this.$message.success('重启成功')
        }).catch(err => {
          console.log(err)
        })
      console.log('after applyNasConfig')
    }

  }
}

</script>

