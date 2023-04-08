<template>
  <div style="display: flex;flex-direction: column;align-items: center;">
    <input
        ref="pictureInput"
        type="file"
        accept="image/jpg, image/jpeg, image/png"
        @change="selectFile"
        hidden
    />
    <button @click="connect">连接服务器</button>
    <input v-model="text" placeholder="用户输入"/>
    <button @click="funcA">执行方法A</button>
    <button @click="funcB">执行方法B</button>
    <button @click="funcB_">执行方法B_</button>
    <button @click="funcC">执行通知方法C(发送图片)</button>
    <button @click="funcD">执行通知方法D</button>
    <button @click="closeSocket()">浏览器关闭连接</button>
    <button @click="serverClose">服务端踢出连接</button>
  </div>
</template>

<script setup>
import {closeSocket, newSocket, socket} from "@/websocketBrowser";
import {computed, ref, watch} from "vue";

const token = 'zhao'
const path = 'ws://127.0.0.1:8080'
const text = ref('')

const pictureInput = ref('')
const picture = ref('')
function selectFile(e) {
  if (!e.target.files) return
  picture.value = e.target.files[0]
  e.target.value = ''
}

async function connect() {
  await newSocket(token, path)//token非必须，目的是为了帮助开发者识别用户。如果使用setWebsocketPath()指定路径后则path不必要，否则必要。
  console.log('服务器连接成功')
}

async function funcA() {
  const res = await socket('/demo/funcA', JSON.stringify({text: text.value}))//如果想用参数名来承接参数就需要转化为JSON格式进行发送
  console.log(res.data)
}

async function funcB() {
  const data = {
    text:text.value,
    time:Date.now()
  }
  const res = await socket('/demo/funcB', JSON.stringify({data: data}))//可以自动将参数出入到类中，但是类中的参数名需要对应
  console.log(res.data)
}

async function funcB_(){
  const res = await socket('/demo/funcB_', text.value)//本条展示如何发送不经格式化的数据
  console.log(res.data)
}

function funcC() {
  pictureInput.value.click()
  const flag = watch(()=>picture.value,async (blob) => {
    const res = await socket('/demo/funcC', JSON.stringify({text: text.value}),new Uint8Array(await blob.arrayBuffer()))//发送图片等文件数据需要先转化为8位无符号整数的类型化数组，当然如果有需求可以自行包装websocketBrowser文件
    console.log(res.data)
    flag()
  })

}

async function funcD() {
  const res = await socket('/demo/funcD', JSON.stringify({text: text.value}))//一般来说会在收到通知会在响应之前，有需求可以自行更改
  console.log(res.data)
}

async function serverClose(){
  await socket('/demo/close')
}
</script>
