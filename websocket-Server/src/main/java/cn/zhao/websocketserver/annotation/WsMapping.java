package cn.zhao.websocketserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为控制器类内需要注册的方法添加WsMapping标签以在项目启动时将控制器类中方法注册于WebsocketServer
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WsMapping {
    /**
     * 方法映射路径，如控制器映射路径为One，方法映射路径为Two，则前端映射该方法路径为OneTwo
     */
    String value();
}