package com.example.demo;

import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.exchange.Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.demo.exchange.Utils.getParmClass;

/**
 * @author Administrator
 * @date 2018/8/17 0017
 */
@RestController
@RequestMapping("/api/")
public class HellWordController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String invoke() {
        return "HelloWord";
    }

    // http://127.0.0.1:8080/api/invoke?address=zookeeper://192.xxx.xxx.xxx:2181&interfaceName=com.xxx.xxx
    // .xxx.xxx.xxx.xxx&method=getGoodsByID&parameterTypes=java.lang
    // .Integer&parameters=237&version=3.1.2.0
    @RequestMapping(value = "/invoke", method = RequestMethod.GET)
    public Object invoke(String address, String interfaceName, String method,
                         String[] parameterTypes, String[] parameters, String version) {

        try {
            parameterTypes = Optional.ofNullable(parameterTypes).orElseGet(() -> getParmClass(interfaceName, method));
        } catch (Exception e) {
            e.printStackTrace();
        }
        GenericService service = Utils.getService(address, interfaceName, version);

        Object o = service.$invoke(method, parameterTypes, Utils.convertParameters(parameterTypes, parameters));
        return o;
    }

}
