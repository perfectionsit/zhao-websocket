import { createApp } from 'vue'
import App from './App.vue'
import {addNoticeFunction, getCloseType, setInit} from "@/websocketBrowser";
//定义连接断开时执行方法
const closeFunc = ()=>{
    console.log('连接已断开，断开类型为：'+getCloseType())
}
//定义通知方法
const NoticeFunctionA =(data)=>{
    console.log('接收到通知C，通知信息长度为'+data.length)
}
const NoticeFunctionB =(data)=>{
    console.log('接收到通知D，通知信息为'+data)
}
//注册连接断开时执行方法
setInit(closeFunc)
//注册通知方法
addNoticeFunction(NoticeFunctionA,'NoticeFunctionA')
addNoticeFunction(NoticeFunctionB,'NoticeFunctionB')

createApp(App).mount('#app')
