package com.example.serverdemo;

import cn.zhao.websocketserver.WsEvent;
import jakarta.websocket.Session;
import org.springframework.stereotype.Component;

@Component
//事件监听器，没有也不影响。
public class Event implements WsEvent {
    @Override
    public void onOpen(Session session) throws Exception {
        System.out.println("新连接接入，id为:"+session.getId());
    }

    @Override
    public void onClose(Session session) throws Exception {
        System.out.println("连接断开，id为:"+session.getId());
    }

    @Override
    public void onError(Session session, Throwable throwable) throws Exception {
        System.out.println("连接错误，id为:"+session.getId()+",错误信息为："+throwable.getMessage());
    }
}
