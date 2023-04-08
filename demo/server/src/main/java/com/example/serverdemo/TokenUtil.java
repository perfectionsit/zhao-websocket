package com.example.serverdemo;

import cn.zhao.websocketserver.pojo.WsToken;
import jakarta.websocket.Session;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
//管理token与session
public class TokenUtil {
    private final Map<WsToken, Session> tokenToSessionMap = new ConcurrentHashMap<>();
    public void setToken(WsToken token,Session session){
        tokenToSessionMap.put(token,session);
    }
    public Session getSession(WsToken token){
        return tokenToSessionMap.get(token);
    }
}
