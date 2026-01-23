<template>
    <h1> message page  {{input}}</h1>

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