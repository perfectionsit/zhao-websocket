package cn.zhao.websocketserver;
import jakarta.websocket.Session;
/**
 * 实现该类并为实现类加以@Component标签以处理相应Websocket事件
 */
public interface WsEvent {
    void onOpen(Session session) throws Exception;
    void onClose(Session session) throws Exception;
    void onError(Session session,Throwable e) throws Exception;
}