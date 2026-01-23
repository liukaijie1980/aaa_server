<template>
    <el-container>
        <el-header style="height: fit-content;">
             
            <el-tag type="info">节点名称:   {{currentLabel}}</el-tag>
            <el-tag type="info">创建者:     {{owner}}</el-tag>
            <el-tag type="info">节点id:     {{currentId}}</el-tag>
       
            <nodestatistic :NodeId="currentId"></nodestatistic>
        </el-header>
        <el-main style="align-items: center;">

            <admin :NodeId="currentId"></admin>
          
            <realm ></realm>
        </el-main>
    </el-container>

</template>

<script>

import admin from "./admin.vue";
import realm from "./realm.vue";
import nodestatistic from "./nodestatistic.vue";
import { GetNodeStatistic } from '@/api/NodeStatistic'

export default {
    props: ['NodeId'],

    // 组件注册
    components: {      
        admin,
        realm,
        nodestatistic,
    },
    data() {
        return {
           currentLabel: this.$store.getters.treeObj.getCurrentNode().label,
            owner:this.$store.getters.treeObj.getCurrentNode() .owner,
           currentId:this.$store.getters.treeObj.getCurrentNode() .id,          

        }
       
    },

   

}

</script>



<style lang="less" scoped>

</style>








      
      
 