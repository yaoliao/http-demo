package com.example.demo.exchange;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 * @date 2018/8/17 0017
 */
public class Utils {

    private static ApplicationConfig applicationConfig;

    private static final Map<String, GenericService> referenceMap = new ConcurrentHashMap<>();
    private static final Map<String, RegistryConfig> registryMap = new ConcurrentHashMap<>();

    static {
        applicationConfig = new ApplicationConfig();
        applicationConfig.setName("dubbo-http");
    }


    public static String[] getParmClass(String className, String methodName) {
        final List<String> list = new ArrayList<>();
        try {
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getMethods();
            Arrays.stream(methods).forEach(m -> {
                if (m.getName().equals(methodName)) {
                    Class<?>[] types = m.getParameterTypes();
                    Arrays.stream(types).forEach(e -> list.add(e.getName()));
                }
            });

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list.toArray(new String[]{});
    }

    // zookeeper://192.168.100.141:2181
    public static RegistryConfig getRegistry(String address) {
        RegistryConfig registry = registryMap.get(address);
        if (registry == null) {
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(address);
            registryMap.putIfAbsent(address, registryConfig);
            registry = registryMap.get(address);
        }
        return registry;
    }

    public static GenericService getService(String address, String interfaceName, String version) {
        RegistryConfig registry = getRegistry(address);
        String key = interfaceName + version;
        GenericService referenceConfig = referenceMap.get(key);
        if (referenceConfig == null) {
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setApplication(applicationConfig);
            reference.setRegistry(registry);
            reference.setInterface(interfaceName);
            reference.setVersion(version);
            reference.setGeneric(true);
            reference.setProtocol("dubbo");

            GenericService genericService = reference.get();

            referenceMap.putIfAbsent(key, genericService);
            referenceConfig = referenceMap.get(key);
        }
        return referenceConfig;
    }

    public static Object[] convertParameters(String[] parameterTypes, String[] parameters) {

        Object[] objects = null;
        try {
            objects = new Object[parameters.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Object o = parameters[i];
                String parameterType = parameterTypes[i];
                boolean primitive = false;
                try {
                    primitive = isPrimitive(Class.forName(parameterType));
                    if (!primitive) {
                        o = convertMap(JSONObject.parseObject(parameters[i]));

                    }
                } catch (ClassNotFoundException e) {
                    o = convertMap(JSONObject.parseObject(parameters[i]));
                }
                objects[i] = o;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objects;
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    public static Map<String, Object> convertMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>(16);
        jsonObject.forEach(map::put);
        return map;
    }


    public static void main(String[] args) {

        String name = int.class.getName();
        System.out.println(name);

        try {
//            Class<?> anInt = Class.forName(name);
            boolean primitive = int.class.isPrimitive();
            String s = primitive ? "yes" : "no";
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*// 引用远程服务
        // 该实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();

        reference.setApplication(applicationConfig);


        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://192.168.100.141:2181");

        reference.setRegistry(registryConfig);


        // 弱类型接口名
        reference.setInterface("com.xxx.XxxService");
        reference.setVersion("1.0.0");
        // 声明为泛化接口
        reference.setGeneric(true);

        // 用com.alibaba.dubbo.rpc.service.GenericService可以替代所有接口引用
        GenericService genericService = reference.get();

        // 基本类型以及Date,List,Map等不需要转换，直接调用
        Object result = genericService.$invoke("sayHello", new String[]{"java.lang.String"}, new Object[]{"world"});

        // 用Map表示POJO参数，如果返回值为POJO也将自动转成Map
        Map<String, Object> person = new HashMap<String, Object>();
        person.put("name", "xxx");
        person.put("password", "yyy");
        // 如果返回POJO将自动转成Map
        //Object result = genericService.$invoke("findPerson", new String[]{"com.xxx.Person"}, new Object[]{person});*/

    }


}
