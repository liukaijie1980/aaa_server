<template>
    <h1> webportal page {{input}}</h1>

</template>  
      
      
      
<script>
export default {
    data() {
        return {
            input: this.NodeId

        }
    },


    props: ['NodeId'],



    methods: {

    }
}

</script>