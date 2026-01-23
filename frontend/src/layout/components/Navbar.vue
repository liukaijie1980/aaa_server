<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container"
      @toggleClick="toggleSideBar" />
    <breadcrumb id="breadcrumb-container" class="breadcrumb-container" />
    <div class="right-menu">
      <template v-if="device !== 'mobile'">

        <Nas ref="childNas" />
        <DbInformation ref="childDbInformation" />
        <!-- 奇怪，把下面的按钮代码放到上面的自定义组件中去的话，每个按钮会独占一行，所以被迫分离出来 -->
        <el-button type="primary" plain size="mini" @click="showNasDialog">NAS对接</el-button>
        <el-button type="primary" plain size="mini" @click="showDbInfoDialog">数据库信息</el-button>
        <el-button type="primary" plain size="mini">统计报表</el-button>
        <el-button type="primary" plain size="mini">抓包分析</el-button>
        <search id="header-search" class="right-menu-item" />
        <error-log class="errLog-container right-menu-item hover-effect" />
        <screenfull id="screenfull" class="right-menu-item hover-effect" />
        <el-tooltip content="Global Size" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>

      </template>


      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
        <div class="avatar-wrapper">
           <!--
            <img :src="avatar + '?imageView2/1/w/80/h/80'" class="user-avatar">
            -->
          <i class="el-icon-caret-bottom" />
        </div>
        <el-dropdown-menu slot="dropdown">
          <!-- 
          <router-link to="/profile/index">
            <el-dropdown-item>Profile</el-dropdown-item>
          </router-link>
          <router-link to="/">
            <el-dropdown-item>Dashboard</el-dropdown-item>
          </router-link>
          <a target="_blank" href="https://github.com/PanJiaChen/vue-element-admin/">
            <el-dropdown-item>Github</el-dropdown-item>
          </a>
          <a target="_blank" href="https://panjiachen.github.io/vue-element-admin-site/#/">
            <el-dropdown-item>Docs</el-dropdown-item>
          </a>
        -->
          <el-dropdown-item @click.native="showVersionInfo">版本信息</el-dropdown-item>


          <el-dropdown-item divided @click.native="logout">
            <span style="display:block;">Log Out</span>
          </el-dropdown-item>

        </el-dropdown-menu>
      </el-dropdown>

    </div>

    <el-dialog :visible.sync="versionDialogVisible" title="版本信息">
      <p>前端Git Version: {{ gitVersion }}</p>
      <p>前端Commit Hash: {{ gitCommitHash }}</p>
      <p>后端Git Version: {{ backendGitVersion }}</p>

      <span slot="footer" class="dialog-footer">
        <el-button @click="versionDialogVisible = false">确定</el-button>
      </span>
    </el-dialog>


  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import ErrorLog from '@/components/ErrorLog'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import Search from '@/components/HeaderSearch'
import DbInformation from './globalDialog/DbInformation.vue'
import Nas from './globalDialog/Nas.vue'
import { getGit } from '@/api/backend-version'

export default {
  components: {
    Breadcrumb,
    Hamburger,
    ErrorLog,
    Screenfull,
    SizeSelect,
    Search,
    DbInformation,
    Nas
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar',
      'device'
    ])
  },

  data() {
    return {
      msg: '',
      versionDialogVisible: false, // 对话框是否显示的状态
      gitVersion: process.env.GIT_VERSION,
      gitCommitHash: process.env.GIT_COMMIT_HASH,
      backendGitVersion:'',
    }
  },

  methods: {

   

    // ...其他方法...
    showVersionInfo() {

      getGit().then(Response  => {
        console.log("Response",Response.data.data );  // Here you have your commit.id
        this.backendGitVersion = Response.data.data.shortCommitId ;
      });
      
      this.versionDialogVisible = true;

    },
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      await this.$store.dispatch('user/logout')
      this.$router.push(`/login?redirect=${this.$route.fullPath}`)
    },
    showDbInfoDialog: function () {

      console.log("showDbDialog", this.msg);
      this.$refs.childDbInformation.parentMsg(this.msg);

    },
    showNasDialog: function () {

      console.log("showNasDialog", this.msg);
      this.$refs.childNas.parentMsg(this.msg);

    },

  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  // overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, .08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
