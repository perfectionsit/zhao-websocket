package cn.zhao.websocketserver.annotation;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 为控制器类添加WsController标签以在项目启动时将控制器类中方法注册于WebsocketServer
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface WsController {
    /**
     * 控制器映射路径，如控制器映射路径为One，方法映射路径为Two，则前端映射该方法路径为OneTwo
     */
    String value() default "";
}