package com.example.serverdemo;

import cn.zhao.websocketserver.WsAuthentication;
import cn.zhao.websocketserver.pojo.WsRequestBody;
import cn.zhao.websocketserver.pojo.WsToken;
import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import org.springframework.stereotype.Component;

@Component
//监听器，没有也不影响
public class Listener implements WsAuthentication {
    @Resource
    TokenUtil tokenUtil;
    @Override
    public boolean authentication(Session session, WsRequestBody requestBody) throws Exception {
        WsToken token = requestBody.getToken();
        tokenUtil.setToken(token,session);
        System.out.println("token=" + requestBody.getToken() + ", method='" + requestBody.getMethod() + ", requestData='" + requestBody.getRequestData()+", hasFile:"+ requestBody.isHasFile()+", fileLength:"+ (requestBody.isHasFile()?requestBody.getFile().length:0));
        return true;
    }
}
