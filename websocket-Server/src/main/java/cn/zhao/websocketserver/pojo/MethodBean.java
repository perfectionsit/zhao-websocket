package cn.zhao.websocketserver.pojo;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
/**
 * 被@WsMapping标记方法注册的方法POJO
 */
public class MethodBean {
    /**
     * 该方法Bean是否没有记录数据
     */
    private boolean empty = true;
    /**
     * 方法
     */
    private Method method ;
    /**
     * 方法实例
     */
    private Object bean;
    /**
     * 方法入参
     */
    Parameter[] parameters;
    public Method getMethod() {
        return method;
    }
    public Object getBean() {
        return bean;
    }
    public Parameter[] getParameters() {
        return parameters;
    }
    public MethodBean setMethodBean(Method method, Object bean, Parameter[] parameters) {
        this.method = method;
        this.bean = bean;
        this.parameters = parameters;
        this.empty = false;
        return this;
    }
    public boolean isEmpty() {
        return empty;
    }
    @Override
    public String toString() {
        return "MethodBean{" +
                "empty=" + empty +
                ", method=" + method +
                ", bean=" + bean +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}