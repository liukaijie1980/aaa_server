<template>
  <div class="root">
    <div class="search-container" style="display:flex;">
      <el-input placeholder="请输入上网账号" prefix-icon="el-icon-search" v-model="SearchInput">
      </el-input>
      <el-button icon="el-icon-search" circle @click="searchAccountInTree"></el-button>
    </div>
    <div class="custom-tree-container" style="height:100%;display:block ">
      <el-scrollbar style="height:100%;display:block ">

        <el-tree ref="tree" class="flow-tree" :data="dataForTree" node-key="id" :highlight-current=true
          :default-expand-all=true :expand-on-click-node=false :check-on-click-node=true
          @node-click="handleClickNodeEvent" style="width:max-content;min-width: 100%; display: block;height:100%;">

          <div class="custom-tree-node" slot-scope="{ node, data }">
            <!-- type=0,文件节点;type=1,闭合文件夹节点;type=2,展开文件夹节点;type>3,各种功能菜单，详见下面新建菜单代码 -->
            <!-- 渲染菜单左面小图标 -->
            <span v-if="data.type == 1 && node.expanded">
              <i class="el-icon-folder-opened"></i>
            </span>
            <span v-else-if="data.type == 1 && !node.expanded">
              <i class="el-icon-folder"></i>
            </span>
            <span v-else>
              <i class="el-icon-document"></i>
            </span>
            <span>{{ node.label }}</span>
            <!-- 渲染菜单右边小按钮 -->
            <span v-if="data.type < 3">
              <span v-if="data.type == 1">
                <el-button type="primary" size="mini" icon="el-icon-folder-add" circle @click="() => appendFolder(data)">
                </el-button>
                <el-button type="primary" size="mini" icon="el-icon-document-add" circle @click="() => append(data)">
                </el-button>
              </span>

              <el-button type="success" size="mini" icon="el-icon-edit" circle @click.stop="() => rename(node, data)">
              </el-button>
              <el-button type="danger" size="mini" icon="el-icon-document-delete" circle
                @click="() => remove(node, data)">
              </el-button>
            </span>
          </div>
        </el-tree>
      </el-scrollbar>
    </div>
    <!-- 节点/文件夹重命名对话框 -->
    <el-dialog title="重命名" :visible.sync="renameDialogVisible" width="50%" @close="renameDialogClosed">
      <el-form :model="renameForm" :rules="renameFormRules" ref="renameFormRef" label-width="70px">
        <el-form-item label="节点名">
          <el-input v-model="renameForm.name"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="renameDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="renameNodeConfirm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getNodeIdsByAcccount } from '@/api/sideBarTree'

import { v4 as uuidv4 } from 'uuid';

export default {
  computed: {

    dataForTree() {
      console.log("layout->sidebartree apply data to tree", this.$store.state.sideBarTree.data)
      return JSON.parse(JSON.stringify(this.$store.state.sideBarTree.data))

    },

  },


  mounted() {
    //本函数在初始化的最后阶段调用
    console.log("mounted dispatch before")
    this.$store.dispatch('sideBarTree/GetTree', this.$store.getters.nodeid)
    console.log("mounted dispatch finish", this.$store.state.sideBarTree.data)
    //保存树组件对象到store
    this.$store.commit('sideBarTree/SET_TREE_OBJ', this.$refs.tree)

  },


  data() {
    return {
      SearchInput: '',
      renameDialogVisible: false,
      renameForm: {
        name: '',

      },
      CurrentNodeData: {},
      renameFormRules: {},
    }
  },



  methods: {
    searchAccountInTree() {
      // 折叠所有树节点
      for (let node of this.$refs.tree.store._getAllNodes()) {
        node.expanded = false;
      }

      // 从后端获取需要展开的节点 IDs
      getNodeIdsByAcccount(this.SearchInput).then(Response => {
        const expandNodeIds = Response.data.data; // 需要展开的节点 IDs
        console.log("getNodeIdsByAcccount(this.SearchInput):", expandNodeIds);

        // 遍历所有树节点
        for (let node of this.$refs.tree.store._getAllNodes()) {
          // 如果当前节点在需要展开的节点 IDs 列表中
          if (expandNodeIds.includes(node.data.id)) {
            console.log("match", node.data.id);
            // 展开当前节点及其所有父节点
            let currentNode = node;
            while (currentNode) {
              currentNode.expanded = true;
              currentNode = currentNode.parent;
            }
          }
        }
      });

    },


    handleClickNodeEvent(data, node, item) {
      console.log("data,node,item", data, node, item)

      let NodeId = data.id
      switch (data.type) {
        case 0:
          this.$router.push({ name: 'leaf', params: { NodeId } })
          break;
        case 1:
        case 2:
          this.$router.push({ name: 'folder', params: { NodeId } })
          break;

        case 4:
          this.$router.push({ name: 'account', params: { NodeId } })
          break;
        case 5:
          this.$router.push({ name: 'offlineuser', params: { NodeId } })
          break;
        case 6:
          this.$router.push({ name: 'messages', params: { NodeId } })
          break;
        case 7:
          this.$router.push({ name: 'webportal', params: { NodeId } })
          break;
        case 8:
          this.$router.push({ name: 'onlineuser', params: { NodeId } })
          break;
        case 9:
          this.$router.push({ name: 'authlog', params: { NodeId } })
          break;
      }
    },

    //增加管理节点，该节点下有功能菜单
    append(data) {
      let id = uuidv4()
      console.log("id=", id)
      const newChild = {

        pid: data.id,
        id: id,
        label: "new item",
        type: 0,
        owner: this.$store.getters.name,
        children: [
          //添加功能菜单，这里将来要结合权限管理
          {
            id: uuidv4(),
            label: "认证日志",
            type: 9,
            pid: id,
          },
          {
            id: uuidv4(),
            label: "上网记录",
            type: 5,
            pid: id,
          },
          {
            id: uuidv4(),
            label: "上网账号",
            type: 4,
            pid: id,
          }, /* {
            id: uuidv4(),
            label: "短信群发",
            type: 6,
            pid: id,
          }, {
            id: uuidv4(),
            label: "页面设置",
            type: 7,
            pid: id,
          }, */
          {
            id: uuidv4(),
            label: "在线用户",
            type: 8,
            pid: id,
          },


        ]
      };
      if (!data.children) {
        this.$set(data, "children", []);
      }
      data.children.push(newChild);
      console.log("append", JSON.stringify(data));
      //将变化保存到后端数据库
      this.$store.dispatch('sideBarTree/SetTree', this.$refs.tree.root)

    },
    //增加文件夹节点
    appendFolder(data) {
      const newChild = {
        pid: data.id,
        id: uuidv4(),
        label: "new folder",
        type: 1,
        owner: this.$store.getters.name,
        children: []
      };

      if (!data.children) {
        this.$set(data, "children", []);
      }
      data.children.push(newChild);

      console.log("appendFolder", JSON.stringify(data));
      //将变化保存到后端数据库
      this.$store.dispatch('sideBarTree/SetTree', this.$refs.tree.root)

    },

    //删除节点
    async remove(node, data) {

      console.log("this.$refs.tree.root.id", this.$refs.tree.root.id);
      console.log("node.parent.id", node.parent.id);
      //if ((data.type==1 && !node.isLeaf) || (this.$store.getters.rootPid=== data.pid) )      
      if ((data.type == 1 && !node.isLeaf) || (this.$refs.tree.root.id === node.parent.id)) {
        this.$message({
          showClose: false,
          message: '不能删非空的文件夹或者根节点',
          type: "'error'",
          duration: 5000
        });
        return;
      }
      /* console.log("this.$store.getters.name", this.$store.getters.name);
      console.log("data.owner", data.owner);
      if (this.$store.getters.name !== data.owner ) {
        this.$message({
          showClose: false,
          message: '只有该节点的创建者才能删除该节点',
          type: "'error'",
          duration: 5000
        });
        return;
      } */

      // 弹框提示用户是否删除
      const confirmResult = await this.$confirm(
        '此操作将永久删除该节点及其关联的管理员, 是否继续?',
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

      const parent = node.parent;
      const children = parent.data.children || parent.data;
      const index = children.findIndex(d => d.id === data.id);
      children.splice(index, 1);
      console.log("remove", JSON.stringify(data));
      //将变化保存到后端数据库  
      this.$store.dispatch('sideBarTree/SetTree', this.$refs.tree.root)

    },

    //重命名节点
    rename(node, data) {

      console.log("before rename", JSON.stringify(data));


      this.renameForm.name = data.label;
      this.CurrentNodeData = data;
      this.renameDialogVisible = true;

    },

    renameDialogClosed() { },
    renameNodeConfirm() {
      this.CurrentNodeData.label = this.renameForm.name;
      //将变化保存到后端数据库
      this.$store.dispatch('sideBarTree/SetTree', this.$refs.tree.root)
      this.renameDialogVisible = false;

    },



  },


};


</script>


<style>
.custom-tree-container {
  border: 5px solid #E7E7E7;
  height: 100%;
}

.flow-tree {
  overflow: auto;
  height: 100%;
  margin: 0px;
  font-size: 15px;

}

.custom-tree-node {
  padding: 0px;
}

.flow-tree .el-button {
  padding: 0px;
  margin: 1px;

}

.operate-btns {

  padding: 0px;
  margin: 0px;
}


.el-tree-node:focus>.el-tree-node__content {
  /*设置选中的样式 */
  background-color: #dde9ff !important;
}

.el-tree-node__content:hover {
  /*设置鼠标飘过的颜色 */
  background: #eaf9ff !important;
  color: #007bff;
}

.el-tree--highlight-current .el-tree-node.is-current>.el-tree-node__content {
  /*current选中的样式 */
  color: #4d95fd;
  font-weight: bold;
  background-color: #dde9ff !important;
}
</style>

