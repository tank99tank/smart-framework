package org.uushang.gateway.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import reactor.core.publisher.Mono;

@RestController
@Validated
public class TestController {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Mono<JSONObject> test() {

    	JSONObject o = new JSONObject();
    	o.put("test", System.currentTimeMillis());
    	
        return Mono.just(o);
    }

}