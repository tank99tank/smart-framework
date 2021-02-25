package org.uushang.gateway.security;

import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.uushang.gateway.config.annotation.Param;
import org.uushang.gateway.config.annotation.SessionParam;
import org.uushang.gateway.config.annotation.SessionParamAutowired;
import org.uushang.gateway.security.config.JwtOauth2ClientProperties;
import org.uushang.gateway.security.util.Rests;
import org.uushang.gateway.util.Platforms;

import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;

/**
 *
 * 调用4A接获取权限信息
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-11-12 11:05:12
 * @copyright www.liderong.cn
 */
@RestController
@RequestMapping("/authority")
@Validated
public class AuthorityClientHandler {

    @Autowired
    private JwtOauth2ClientProperties properties;

    /**
     * 获取应用下某用户的菜单树状结构列表
     *
     * @param accessToken 用户鉴权的token
     * @param platform 来源系统
     * @param userId 用户ID
     * @return
     */
    @RequestMapping(value = "/getMenuTree", method = RequestMethod.GET)
    @SessionParamAutowired
    public Mono<JSONObject> getMenuTree(@SessionParam(name = Param.ACCESSTOKEN) String accessToken,
                                        @SessionParam(name = Param.PLATFORM) String platform,
                                        @SessionParam(name = Param.USERID) String userId) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
        params.add("accessToken", accessToken);
        params.add("clientId", Platforms.getClientId(properties, platform));
        params.add("userId", userId);

        return Mono.just(Rests.post(properties.getMenuTreeUrl(), params));
    }

    /**
     * 按层级获取用户的菜单权限
     *
     * @param accessToken 用户鉴权的token
     * @param platform 客户端ID
     * @param userId 用户ID
     * @param type 菜单级别 1到9，比如一级菜单就是'1'. 必填项
     * @param sequence 菜单的序号
     * @return
     */
    @RequestMapping(value = "/getPermissions", method = RequestMethod.GET)
    @SessionParamAutowired
    public Mono<JSONObject> getUserPermissions(@SessionParam(name = Param.ACCESSTOKEN) String accessToken,
                                           @SessionParam(name = Param.PLATFORM) String platform,
                                           @SessionParam(name = Param.USERID) String userId,
                                           @RequestParam(name = "type")
                                           @NotBlank(message = "type can't be null") String type,
                                           @RequestParam(name = "sequence", required = false) String sequence) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("accessToken", accessToken);
        params.add("clientId", Platforms.getClientId(properties, platform));
        params.add("userId", userId);
        params.add("type", type);
        params.add("id", sequence);

        return Mono.just(Rests.post(properties.getPermissionsUrl(), params));
    }
}