package cn.zhao.websocketserver;
import cn.zhao.websocketserver.pojo.WsRequestBody;
import jakarta.websocket.Session;
/**
 * 实现该类并为实现类加以@Component标签以处理Websocket信息鉴权（拦截器）
 */
public interface WsAuthentication {
    /**
     * 拦截请求同时鉴权session与WsRequestBody
     * @return 放行请求返回true，否则返回false
     */
    boolean authentication(Session session, WsRequestBody requestBody) throws Exception;
}