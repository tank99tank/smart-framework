package org.uushang.gateway.security.util;

import com.alibaba.fastjson.JSONObject;
import com.owinfo.config.result.Result;
import com.owinfo.config.result.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 *
 * http接口调用
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-11-12 11:21:58
 * @copyright www.liderong.cn
 */
@Component
@Slf4j
public class Rests {

    private static RestTemplate rest;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    private void construct() {
        Rests.setRestTemplate(restTemplate);
    }

    private static void setRestTemplate(RestTemplate restTemplate) {
        Rests.rest = restTemplate;
    }

    @Nullable
    public static String post(String url, HttpEntity httpEntity) {
        String str;
        try {
            str = rest.postForObject(url, httpEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return str;
    }

    public static JSONObject post(String url, MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<MultiValueMap<String, String>> formEntity = new HttpEntity<>(params, headers);
        JSONObject resource;
        try {
            resource = rest.postForObject(url, formEntity, JSONObject.class);
        } catch (Exception e) {
            log.error("远程调用失败 {}", e.toString());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", Status.FAILURE);
            jsonObject.put("msg", "Remote request execution fail");
            return jsonObject;
        }
        return resource;
    }
}