<template>
    <el-table :data="tableData" stripe style="width: 100%;">
        <el-table-column prop="totalAccounts" label="总账号数" width="180">
        </el-table-column>
        <el-table-column prop="onlineAccounts" label="当前在线账号数" width="180">
        </el-table-column>
        <el-table-column prop="onlineTerminals" label="当前在线终端数" width="180">
        </el-table-column>
    </el-table>
</template>

<script>

import { GetNodeStatistic } from '@/api/NodeStatistic'

export default {
    props: ['NodeId'],

    data() {
        return {

            tableData: [{

                totalAccounts: 0,
                onlineAccounts: 0,
                onlineTerminals: 0,

            },
            ]

        }

    },

    created() {
        this.getUserList()
    },
    methods: {

        //获取用户列表
        getUserList() {
            console.log("GetNodeStatistic: getCurrentNode", this.$store.getters.treeObj.getCurrentNode())
            GetNodeStatistic(this.$store.getters.treeObj.getCurrentNode().id).then(
                Response => {
                    console.log("after GetNodeStatistic:Response.data.data", Response.data.data)
                    this.tableData = Response.data.data;


                }).catch(err => console.log("GetNodeStatistic error:", err))

            console.log("after GetNodeStatistic");
        },
    }

}

</script>



<style lang="less" scoped></style>








      
      
 