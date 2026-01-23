<template>

  <div>

    <el-card>
      <!--搜索与添加区域-->
      <el-row :gutter="20">
        <el-col :span="4">
          <el-button type="primary" @click="showAddDialog">添加上网账号</el-button>
        </el-col>
        <el-col :span="8">
          <el-input v-model="queryInfo.query" placeholder="请输入账号" clearable @clear="getUserList">
            <el-button slot="append" icon="el-icon-search" @click="getUserList" />
          </el-input>
        </el-col>
        <el-col :span="8">
          <el-select v-model="value" placeholder="请选择 域" @change="selectEventHandler" @remove-tag="selectEventHandler">
            <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-col>

      </el-row>
      <!-- 筛选按钮 -->
      <el-popover placement="bottom" title="筛选列" trigger="click" width="40">
        <el-checkbox-group v-model="checkedColumns" size="mini">
          <el-checkbox v-for="item in checkBoxGroup" :key="item" :label="item" :value="item" />
        </el-checkbox-group>
        <div slot="reference" title="筛选列" class="filter-table-col"><i class="el-icon-c-scale-to-original" /></div>
      </el-popover>

      <!--用户列表区域-->
      <el-table :data="userList" border stripe>
        <el-table-column label="操作" width="120px">
          <template slot-scope="scope">
            <!-- 修改按钮 -->
            <el-tooltip effect="dark" content="修改" placement="top-start" :enterable="false">
              <el-button type="primary" icon="el-icon-edit" size="mini" @click="showEditDialog(scope.$index)" />
            </el-tooltip>
            <!-- 删除按钮 -->
            <el-tooltip effect="dark" content="删除" placement="top-start" :enterable="false">
              <el-button type="danger" icon="el-icon-delete" size="mini" @click="removeUserById(scope.$index)" />
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column type="index" label="#" />
        <el-table-column label="账号" prop="userName" />
        <el-table-column label="域" prop="realm" />
        <el-table-column v-if="colData[0].istrue" label="是否停用" prop="isFrozen" :formatter="isFrozenFormat" />
        <el-table-column v-if="colData[1].istrue" label="生效日期" prop="validDate" />
        <el-table-column v-if="colData[2].istrue" label="失效日期" prop="expireDate" />
        <el-table-column v-if="colData[3].istrue" label="同用限制" prop="simualUseLimit" />
        <el-table-column v-if="colData[4].istrue" label="上行带宽(kbps)" prop="inboundCar" />
        <el-table-column v-if="colData[5].istrue" label="下行带宽(kbps)" prop="outboundCar" />
        <el-table-column v-if="colData[6].istrue" label="最大会话时长" prop="maxSessionTimeout" />
        <el-table-column v-if="colData[7].istrue" label="记账报文间隔" prop="updateInterval" />
        <el-table-column v-if="colData[8].istrue" label="剩余时间（秒）" prop="secondRemain" />
        <el-table-column v-if="colData[9].istrue" label="剩余流量（字节）" prop="byteRemain" />
        <el-table-column v-if="colData[10].istrue" label="最后修改人" prop="adminName" />
        <el-table-column v-if="colData[11].istrue" label="修改日期" prop="modifyDate" />
      </el-table>
      <!--分页区域-->
      <el-pagination
        :current-page="queryInfo.pagenum"
        :page-sizes="[1, 2, 5, 10]"
        :page-size="queryInfo.pagesize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!--添加用户的对话框-->
    <el-dialog title="添加账号" :visible.sync="addDialogVisible" width="50%" @close="addDialogClosed">
      <!--主体区域-->
      <el-form ref="addFormRef" :model="addForm" :rules="FormRules" label-width="200px">
        <el-form-item label="账号" prop="userName">
          <el-input v-model="addForm.userName" />
        </el-form-item>
        <el-form-item label="域" prop="reaml">
          <el-select v-model="addForm.realm" placeholder="请选择 域">
            <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="userPassword">
          <el-input v-model="addForm.userPassword" show-password />
        </el-form-item>
        <el-form-item label="是否停用" prop="isFrozen">
          <el-radio-group v-model="addForm.isFrozen">
            <!-- 注意这里label要加: -->
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="起止日期" prop="date">
          <el-date-picker
            v-model="addForm.validDate"
            type="datetime"
            placeholder="选择生效日期时间"
            align="right"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="pickerOptions"
          />
          <el-date-picker
            v-model="addForm.expireDate"
            type="datetime"
            placeholder="选择失效日期时间"
            align="right"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="pickerOptions"
          />
        </el-form-item>
        <el-form-item label="同用限制" prop="simualUseLimit">
          <el-input v-model="addForm.simualUseLimit" />
        </el-form-item>
        <el-form-item label="上行带宽(kbps)" prop="inboundCar">
          <el-input v-model="addForm.inboundCar" />
        </el-form-item>
        <el-form-item label="下行带宽(kbps)" prop="outboundCar">
          <el-input v-model="addForm.outboundCar" />
        </el-form-item>
        <el-form-item label="最大会话时长" prop="maxSessionTimeout">
          <el-input v-model="addForm.maxSessionTimeout" />
        </el-form-item>
        <el-form-item label="记账报文间隔" prop="updateInterval">
          <el-input v-model="addForm.updateInterval" />
        </el-form-item>
        <el-form-item label="可用时间（秒）" prop="secondRemain">
          <el-input v-model="addForm.secondRemain" />
        </el-form-item>
        <el-form-item label="可用流量（字节）" prop="byteRemain">
          <el-input v-model="addForm.byteRemain" />
        </el-form-item>
      </el-form>
      <!--底部区域-->
      <span slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="addUser">确 定</el-button>
      </span>
    </el-dialog>
    <!--修改用户对话框-->
    <el-dialog title="修改账号" :visible.sync="editDialogVisible" width="50%" @close="editDialogClosed">
      <el-form ref="editFormRef" :model="editForm" :rules="FormRules" label-width="200px">
        <el-form-item label="账号" prop="userName">
          <el-input v-model="editForm.userName" disabled />
        </el-form-item>
        <el-form-item label="域" prop="reaml">
          <el-select v-model="editForm.realm" placeholder="请选择 域" disabled>
            <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="userPassword">
          <el-input v-model="editForm.userPassword" show-password />
        </el-form-item>
        <el-form-item label="是否停用" prop="isFrozen">
          <el-radio-group v-model="editForm.isFrozen">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="起止日期" prop="date">
          <el-date-picker
            v-model="editForm.validDate"
            type="datetime"
            placeholder="选择生效日期时间"
            align="right"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="pickerOptions"
            @change="clearEditValidDate"
          />
          <el-date-picker
            v-model="editForm.expireDate"
            type="datetime"
            placeholder="选择失效日期时间"
            align="right"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="pickerOptions"
            @change="clearEditExpireDate"
          />
        </el-form-item>
        <el-form-item label="同用限制" prop="simualUseLimit">
          <el-input v-model="editForm.simualUseLimit" />
        </el-form-item>
        <el-form-item label="上行带宽(kbps)" prop="inboundCar">
          <el-input v-model="editForm.inboundCar" />
        </el-form-item>
        <el-form-item label="下行带宽(kbps)" prop="outboundCar">
          <el-input v-model="editForm.outboundCar" />
        </el-form-item>
        <el-form-item label="最大会话时长" prop="maxSessionTimeout">
          <el-input v-model="editForm.maxSessionTimeout" />
        </el-form-item>
        <el-form-item label="记账报文间隔" prop="updateInterval">
          <el-input v-model="editForm.updateInterval" />
        </el-form-item>
        <el-form-item label="可用时间（秒）" prop="secondRemain">
          <el-input v-model="editForm.secondRemain" />
        </el-form-item>
        <el-form-item label="可用流量（字节）" prop="byteRemain">
          <el-input v-model="editForm.byteRemain" />
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

import { AddAccountInfo, DeleteAccountInfo, UpdateAccountInfo, GetAccountInfo } from '@/api/account'
import { AddRealm, DeleteRealm, UpdateRealm, GetRealms } from '@/api/realm'
import { resolve } from 'path'

export default {
  props: ['NodeId'],

  data() {
    return {
      // 域 选择框
      // 可选项
      options: [{
        value: '',
        label: ''
      }],
      // 选中的项
      value: [''],

      // colData中列出表格中的每一列，默认都展示
      colData: [

        { title: '是否停用', istrue: true },
        { title: '生效日期', istrue: true },
        { title: '失效日期', istrue: true },
        { title: '同用限制', istrue: true },

        { title: '上行带宽', istrue: true },
        { title: '下行带宽', istrue: true },
        { title: '最大会话时长', istrue: false },
        { title: '记账报文间隔', istrue: false },
        { title: '剩余时间', istrue: false },
        { title: '剩余流量', istrue: false },

        { title: '最后修改人', istrue: false },
        { title: '修改日期', istrue: false }
      ],

      // 多选框的列表，列出表格的每一列
      checkBoxGroup: ['是否停用', '生效日期', '失效日期', '同用限制', '上行带宽', '下行带宽', '最大会话时长', '记账报文间隔', '剩余时间', '剩余流量',
        '最后修改人', '修改日期'
      ],
      // 当前选中的多选框，代表当前展示的列
      checkedColumns: ['是否停用', '生效日期', '失效日期', '同用限制', '上行带宽', '下行带宽'],

      // 日期组件参数
      pickerOptions: {
        /*   disabledDate(time) {
            return time.getTime() < date.getTime()-3600 * 1000 * 24;
          }, */
        shortcuts: [{
          text: '今天',
          onClick(picker) {
            picker.$emit('pick', new Date())
          }
        }, {
          text: '一天后',
          onClick(picker) {
            const date = new Date()
            date.setTime(date.getTime() + 3600 * 1000 * 24)
            picker.$emit('pick', date)
          }
        }, {
          text: '一周后',
          onClick(picker) {
            const date = new Date()
            date.setTime(date.getTime() + 3600 * 1000 * 24 * 7)
            picker.$emit('pick', date)
          }
        }, {
          text: '一月后',
          onClick(picker) {
            const date = new Date(new Date().setMonth(new Date().getMonth() + 1))

            picker.$emit('pick', date)
          }
        }, {
          text: '一年后',
          onClick(picker) {
            const date = new Date()
            date.setFullYear(date.getFullYear() + 1)

            picker.$emit('pick', date)
          }
        }
        ]
      },

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
      // 添加用户的表单数据
      addForm: {
        userName: '',
        realm: '@',
        userPassword: '',
        authMode: 0,
        isFrozen: false,
        secondRemain: 0,
        byteRemain: 0,
        simualUseLimit: 1,
        maxSessionTimeout: 0,
        inboundCar: 0,
        outboundCar: 0,
        qosProfile: '',
        updateInterval: 1800,
        adminName: this.$store.getters.name,
        validDate: '',
        expireDate: ''

      },
      // 添加表单验证规则对象
      FormRules: {
        userName: [
          {
            required: true,
            message: '请输入用户名',
            trigger: 'blur'
          },
          {
            min: 1,
            max: 40,
            message: '用户名的长度在1-40个字符之间',
            trigger: 'blur'
          }
        ],
        userPassword: [
          {
            required: true,
            message: '请输入密码',
            trigger: 'blur'
          },
          {
            min: 1,
            max: 40,
            message: '密码的长度在1-40个字符之间',
            trigger: 'blur'
          }
        ]

      },
      // 修改用户对话框
      editDialogVisible: false,
      editForm: {
        userName: '',
        realm: '',
        userPassword: '',
        authMode: 0,
        isFrozen: false,
        secondRemain: 0,
        byteRemain: 0,
        simualUseLimit: 0,
        maxSessionTimeout: 0,
        inboundCar: 0,
        outboundCar: 0,
        qosProfile: '',
        updateInterval: 0,
        adminName: this.$store.getters.name,
        validDate: '',
        expireDate: ''
      }

    }
  },
  watch: {
    checkedColumns(val) {
      const arr = this.checkBoxGroup.filter(i => !val.includes(i))
      this.colData.filter(i => {
        if (arr.indexOf(i.title) != -1) {
          i.istrue = false
        } else {
          i.istrue = true
        }
      })
      this.reload = Math.random()
    }
  },
  mounted() {
    new Promise((resolve, reject) => {
      console.log('mounted:getRealmList()')
      this.getRealmList(resolve, reject)
    }).then(() => { // 上一个接口成功resolve之后执行then()
      console.log('mounted:getUserList()')
      this.getUserList()
    })
  },

  methods: {
    clearEditValidDate(v) {
      // 点清空按钮
      if (v === null) {
        this.editForm.validDate = ''
      }
    },
    clearEditExpireDate(v) {
      // 点清空按钮
      if (v === null) {
        this.editForm.expireDate = ''
      }
    },

    showAddDialog() {
      this.addDialogVisible = true
      this.addForm.realm = this.value
    },
    selectEventHandler() {
      // 域被重新选择，要刷新账号数据
      this.getUserList()
    },
    // 获取realm列表
    getRealmList(resolve, reject) {
      console.log('GetRealms: getCurrentNode', this.$store.getters.treeObj.getCurrentNode())
      GetRealms(this.$store.getters.treeObj.getCurrentNode().pid).then(
        Response => {
          console.log('after GetRealms:Response.data.data', Response.data.data)
          let res = []
          res = Response.data.data
          if (res.length > 0) {
            // 给选择框设置默认值
            this.value = res[0].realm
          }
          this.options.length = 0
          for (let i = 0; i < res.length; i++) {
            const obj = { value: '', label: '' }
            obj.value = res[i].realm
            obj.label = res[i].realm
            this.options.push(obj)
          }
          resolve()
        }).catch(err => {
        console.log('GetRealms error:', err)
        reject()
      })

      console.log('after GetRealms')
    },

    isFrozenFormat(row, column) {
      if (row.isFrozen) {
        return '是'
      } else {
        return '否'
      }
    },

    // 获取用户列表
    getUserList() {
      console.log('this.value', this.value)           
      GetAccountInfo(this.queryInfo.query.trim(), this.value, this.queryInfo.pagenum, this.queryInfo.pagesize)
        .then(
          Response => {
            console.log('after GetAccountInfo:Response.data.data', Response.data.data)
            this.userList = Response.data.data.records
            this.total = Response.data.data.total
          })
        .catch(err => console.log('GetAccountInfo error:', err))

      console.log('after GetAccountInfo')
    },
    // 监听 pagesize 改变的事件
    handleSizeChange(newSize) {
      this.queryInfo.pagesize = newSize
      this.getUserList()
      console.log('handleSizeChange(newSize)', newSize)
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
      this.$refs.addFormRef.resetFields()
    },
    // 点击按钮，添加新用户
    addUser() {
      this.$refs.addFormRef.validate(async valid => {
        if (!valid) return
        // 可以发起添加用户的网络请求

        console.log('AddAccountInfo: getCurrentNode', this.$store.getters.treeObj.getCurrentNode())
        console.log('AddAccountInfo: this.addForm', this.addForm)
        AddAccountInfo(this.addForm)
          .then(Response => {
            console.log('AddAccountInfo finish')
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

      console.log('after AddAccountInfo')
    },

    // 修改用户
    async showEditDialog(id) {
      console.log('showEditDialog:id', id)
      console.log('showEditDialog:this.userList[id]', this.userList[id])
      this.editForm = this.userList[id]

      this.editDialogVisible = true
    },

    // 监听修改用户对话框的关闭事件
    editDialogClosed() {
      // ！！！奇怪，启用下面代码会污染用户列表
      //  this.$refs.editFormRef.resetFields()
    },

    // 修改用户信息并提交
    editUserInfo() {
      this.$refs.editFormRef.validate(async valid => {
        if (!valid) return
        // 发起修改用户信息的数据请求
        UpdateAccountInfo(this.editForm)
          .then(Response => {
            console.log('UpdateAccountInfo finish')
            // 更新成功
            this.$message.success('更新成功')
            // 刷新用户列表
            this.getUserList()
            // 关闭对话框
            this.editDialogVisible = false
          }).catch(err => {
            console.log(err)
          })
        console.log('after UpdateAccountInfo')
      })
    },
    // 根据id删除对应的用户信息
    async removeUserById(id) {
      // 弹框提示用户是否删除
      const confirmResult = await this.$confirm(
        '此操作将永久删除该账号, 是否继续?',
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

      DeleteAccountInfo(this.userList[id].userName, this.userList[id].realm)
        .then(Response => {
          console.log('DeleteAccountInfo finish')
          // 更新成功
          this.$message.success('删除成功')

          // 刷新用户列表
          this.getUserList()
        }).catch(err => {
          console.log(err)
        })
      console.log('after DeleteAccountInfo')
    }
  }
}

</script>

<style lang="less" scoped>

</style>

