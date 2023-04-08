package cn.zhao.websocketserver.pojo;
/**
 * WebsocketTokenPOJO
 */
public record WsToken(String token) {
    @Override
    public String toString() {
        return token;
    }
}