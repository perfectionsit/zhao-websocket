package com.example.serverdemo;

import cn.zhao.websocketserver.WsUtil;
import cn.zhao.websocketserver.annotation.WsController;
import cn.zhao.websocketserver.annotation.WsMapping;
import cn.zhao.websocketserver.pojo.WsRequestBody;
import cn.zhao.websocketserver.pojo.WsToken;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;

import java.io.IOException;

@WsController("/demo")//可以不选择路径,直接@WsController。这样路径就直接为方法路径
public class Controller {
    @Resource
    TokenUtil tokenUtil;

    @WsMapping("/funcA")
    public String funcA(String text) {
        return text + "执行方法A";
    }

    @WsMapping("/funcB")
    public String funcB(WsToken token, DemoVo data, WsRequestBody wsRequestBody) {
        return JSONObject.toJSONString(new DemoPo(data.getText(), wsRequestBody.getCount(), data.getTime(), token.toString()));
    }

    @WsMapping("/funcB_")
    public String funcB_(WsRequestBody wsRequestBody) {
        return wsRequestBody.getRequestData() +"执行方法B_";
    }

    @WsMapping("/funcC")
    public String funcC(WsToken token, String text, byte[] file) throws IOException {
        WsUtil.sendNotice(tokenUtil.getSession(token), "NoticeFunctionA", file);
        return text + "执行通知方法C";
    }

    @WsMapping("/funcD")
    public String funcD(WsToken token, String text) throws IOException {
        WsUtil.sendNotice(tokenUtil.getSession(token), "NoticeFunctionB", text);
        return text + "执行通知方法D";
    }

    @WsMapping("/close")
    public void close(WsToken token) throws IOException {
        WsUtil.forcedLogout(tokenUtil.getSession(token));
    }
}
