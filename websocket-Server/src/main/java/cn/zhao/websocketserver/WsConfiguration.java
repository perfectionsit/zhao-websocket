package cn.zhao.websocketserver;

import cn.zhao.websocketserver.annotation.WsController;
import cn.zhao.websocketserver.annotation.WsMapping;
import cn.zhao.websocketserver.pojo.MethodBean;
import cn.zhao.websocketserver.pojo.WsRequestBody;
import jakarta.websocket.Session;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
/**
 * 扫描上下文以注册@WsController标记的类中的方法与authentication、onOpenEvent和onCloseEvent实现类
 */
@Configuration
public class WsConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    private ApplicationContext context;
    public static Map<String, MethodBean> methodMap = new HashMap<>();
    public static MethodBean authentication = new MethodBean();
    public static MethodBean onOpenEvent = new MethodBean();
    public static MethodBean onCloseEvent = new MethodBean();
    public static MethodBean onErrorEvent = new MethodBean();
    @Override
    public void afterSingletonsInstantiated() {
        Map<String, WsAuthentication> authenticationClazz = context.getBeansOfType(WsAuthentication.class);
        if (!authenticationClazz.isEmpty()) {
            for (Map.Entry<String, WsAuthentication> entry : authenticationClazz.entrySet()) {
                try {
                    Method authenticationMethod = entry.getValue().getClass().getMethod("authentication", Session.class, WsRequestBody.class);
                    authenticationMethod.setAccessible(true);
                    authentication.setMethodBean(authenticationMethod, entry.getValue(), null);
                    break;
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Map<String, WsEvent> eventClazz = context.getBeansOfType(WsEvent.class);
        if (!eventClazz.isEmpty()) {
            for (Map.Entry<String, WsEvent> entry : eventClazz.entrySet()) {
                try {
                    Class<? extends WsEvent> eventClass = entry.getValue().getClass();
                    Method onCloseMethod = eventClass.getMethod("onClose", Session.class);
                    Method onOpenMethod = eventClass.getMethod("onOpen", Session.class);
                    Method onErrorMethod = eventClass.getMethod("onError", Session.class,Throwable.class);
                    onCloseMethod.setAccessible(true);
                    onOpenMethod.setAccessible(true);
                    onErrorMethod.setAccessible(true);
                    onOpenEvent.setMethodBean(onOpenMethod,entry.getValue(),null);
                    onCloseEvent.setMethodBean(onCloseMethod,entry.getValue(),null);
                    onErrorEvent.setMethodBean(onErrorMethod,entry.getValue(),null);
                    break;
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Map<String, Object> beans = context.getBeansWithAnnotation(WsController.class);
        for (Map.Entry<String, Object> bean : beans.entrySet()) {
            Class<?> beanType = bean.getValue().getClass();
            String wsControllerPath = Objects.requireNonNull(AnnotatedElementUtils.findMergedAnnotation(beanType, WsController.class)).value();
            Map<Method, WsMapping> annotatedMethods = MethodIntrospector.selectMethods(beanType,
                    (MethodIntrospector.MetadataLookup<WsMapping>) method -> AnnotatedElementUtils.findMergedAnnotation(method, WsMapping.class));
            for (Map.Entry<Method, WsMapping> entry : annotatedMethods.entrySet()) {
                Method method = entry.getKey();
                WsMapping wsMapping = entry.getValue();
                method.setAccessible(true);
                methodMap.put(wsControllerPath + wsMapping.value(), new MethodBean().setMethodBean(method, bean.getValue(), method.getParameters()));
            }
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}