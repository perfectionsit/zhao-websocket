package cn.zhao.websocketserver;
import com.alibaba.fastjson2.JSONObject;
import cn.zhao.websocketserver.pojo.WsRequestBody;
import cn.zhao.websocketserver.pojo.WsToken;
import jakarta.websocket.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Websocket工具类
 */
public class WsUtil {
    /**
     * 强制该Session下线(主动，断开连接且前端连接断开类型为”服务器踢出“)
     */
    static public void forcedLogout(Session session) throws IOException {
        if(session!=null&&session.isOpen()){
            sendClose(session);
            session.close();
        }
    }
    /**
     * 向该Session发送响应数据
     * @param count 前端请求下标
     * @param data  数据
     */
    static public void sendResponse(Session session,int count,Object data) throws IOException {
        if(session!=null&&session.isOpen())session.getBasicRemote().sendBinary(responseEncode("response", count, data));
    }

    /**
     * 向该Session发送通知数据
     * @param session 前端Session
     * @param status 通知方法映射路径（函数名）
     * @param data 数据
     */
    static public void sendNotice(Session session,String status,Object data) throws IOException {
        if(session!=null){
            Map<String ,Object> json = new HashMap<>();
            json.put("status",status);
            json.put("data",data);
            if(session.isOpen())session.getBasicRemote().sendBinary(responseEncode("notice", -1, JSONObject.toJSONString(json)));
        }
    }
    /**
     * 通知该Session下线(被动，前端连接断开类型为”服务器踢出“，但是不断开连接)
     */
    static public void sendClose(Session session) throws IOException {
        if(session!=null&&session.isOpen())session.getBasicRemote().sendBinary(responseEncode("", -1, ""));
    }
    /**
     * 发送Pong信息
     */
    static public void sendPong(Session session) throws IOException {
        if(session!=null&&session.isOpen())session.getBasicRemote().sendBinary(
                ByteBuffer.wrap(new byte[1]));
    }
    /**
     * 将后端响应编码<br/>
     * [1:responseType(response|notice|close)]<br/>
     * [4:count]<br/>
     * [1:dataType(2=null,1=String,0=bytes)]<br/>
     * [dataLength-6:data]
     * @param responseType 要相应的类型(response|notice|close)
     * @param count 前端请求下标
     * @param data 将编码数据,暂时仅支持String与byte[]类型
     * @return  编码后比特数组
     */
    public static ByteBuffer responseEncode(String responseType, int count, Object data) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (responseType.equals("response")) {
            os.write((byte) 1);
        }
        else if (responseType.equals("notice")) {
            os.write((byte) 0);
        }
        else {
            os.write((byte) 2);
        }
        int _count = count;
        for (int i = 0; i < 4; i++) {
            os.write((byte) (_count & (255)));
            _count >>= 8;
        }
        if(data == null){
            os.write(2);
        }
        else if(data instanceof byte[]){
            os.write(0);
            os.write((byte[]) data);
        }else{
            os.write(1);
            os.write((data.toString()).getBytes(StandardCharsets.UTF_8));
        }
        return ByteBuffer.wrap(os.toByteArray());
    }
    /**
     * 将前端请求解码 <br/>
     * [1:tokenLength]<br/>
     * [tokenLength:token]<br/>
     * [4:methodNameLength]<br/>
     * [methodNameLength:methodName]<br/>
     * [4:count]<br/>
     * [1:hasFile](hasFile==1?[4:fileLength][fileLength:file])<br/>
     * [1:hasData](hasData==1?[4:dataLength][dataLength:data])
     * @param data 要解码的字节数组
     * @return Map{method:String,count:int,hasFile:String,(data:String),(file:byte[])}
     */
    public static WsRequestBody requestDecode(byte[] data) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        WsRequestBody wsRequestBody = new WsRequestBody();
        int lastLength = data.length;
        byte[] _tokenLength = new byte[1];
        lastLength -= in.read(_tokenLength);
        if(_tokenLength[0]!=0){
            byte[] _token = new byte[_tokenLength[0]];
            lastLength -= in.read(_token);
            wsRequestBody.setToken(new WsToken(new String(_token, StandardCharsets.UTF_8)));
        }
        byte[] _methodName = new byte[getLength(in)];
        lastLength -= (in.read(_methodName) + 4);
        wsRequestBody.setMethod(new String(_methodName, StandardCharsets.UTF_8));
        wsRequestBody.setCount(getLength(in));
        lastLength -= 4;
        boolean hasFile = false;
        byte[] _hasFile = new byte[1];
        lastLength -= in.read(_hasFile);
        if ((_hasFile[0] & 255) == 1) {
            hasFile = true;
            byte[] file = new byte[getLength(in)];
            lastLength -= (in.read(file) + 4);
            wsRequestBody.setFile(file);
        }
        wsRequestBody.setHasFile(hasFile);
        boolean hasData = false;
        byte[] _hasData = new byte[1];
        lastLength -= in.read(_hasData);
        if ((_hasData[0] & 255) == 1) {
            hasData = true;
            byte[] dataBytes = new byte[getLength(in)];
            lastLength -= (in.read(dataBytes) + 4);
            wsRequestBody.setRequestData(new String(dataBytes, StandardCharsets.UTF_8));
        }
        wsRequestBody.setHasData(hasData);
        return lastLength == 0 ? wsRequestBody : null;
    }
    public static int getLength(ByteArrayInputStream in) throws IOException {
        int length = 0;
        byte[] _length = new byte[4];
        if(in.read(_length)==4){
            for (int i = 0; i < 4; i++) {
                length += ((_length[i] & 255) << (8 * i));
            }
        }
        return length;
    }
}