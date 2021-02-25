package org.uushang.gateway.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.owinfo.config.result.Result;
import com.owinfo.config.result.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.uushang.gateway.config.annotation.Param;
import org.uushang.gateway.config.annotation.SessionParam;
import org.uushang.gateway.config.annotation.SessionParamAutowired;
import org.uushang.gateway.security.config.JwtOauth2ClientProperties;
import org.uushang.gateway.security.token.JwtAccessTokenInfo;
import org.uushang.gateway.security.util.DefaultServerRedirectStrategy;
import org.uushang.gateway.security.util.JwtTokenUtils;
import org.uushang.gateway.security.util.Rests;
import org.uushang.gateway.util.Platforms;

import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * endpoint
 *
 * @author pengjunjie
 * @date 2019-08-29
 */
@RestController
@Validated
public class JwtClientEndpoint {

	private DefaultServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private JwtOauth2ClientProperties properties;

	@Autowired
	private JwtTokenService tokenService;

	/**
	 * 	单点退出
 	 */
	@RequestMapping("/ssoLogout")
	public Mono<Void> ssoLogout(ServerWebExchange webExchange) throws URISyntaxException {
		String ssoLogoutUrl = properties.getSsoLogoutUrl();
		URI uri = new URI(ssoLogoutUrl);
		return redirectStrategy.sendRedirect(webExchange, uri);
	}

	/**
	 * 	客户端退出
	 */
	@RequestMapping("/logout")
	public Mono<Void> logout(@NotBlank(message = "accessToken can't be null")
							 @RequestParam("accessToken") String accessToken) {
		String claims = JwtHelper.decodeAndVerify(accessToken, new MacSigner(properties.getSignKey())).getClaims();
		JSONObject claimsJson = JSON.parseObject(claims);

		String jti = claimsJson.getString("jti");
		Boolean hasKey = redisTemplate.hasKey(jti);
		if (hasKey != null && hasKey) {
			redisTemplate.delete(jti);
		}

		return Mono.empty();
	}

	@RequestMapping("/login")
	@SessionParamAutowired
	public Mono<Result> login(@NotBlank(message = "code can't be null")
							  @RequestParam(name = "code") String code,
							  @SessionParam(name = Param.PLATFORM) String platform) {

		// 获取第三方token进行处理
		String accessToken = tokenService.obtainAccessToken(code, platform);
		if (Objects.isNull(accessToken)) {
			return Mono.just(Result.build(Status.FAILURE, "Failed to obtain access token"));
		}

		// 解析返回的token，解析异常，handler统一处理
		try {
			Map<String, Object> returnValue = JwtTokenUtils.getTokenInfo(accessToken, properties.getSignKey());
			JwtAccessTokenInfo tokenInfo = (JwtAccessTokenInfo) returnValue.get("tokenInfo");
			//获取角色信息
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("accessToken", tokenInfo.getOriginTokenValue());
			params.add("clientId", Platforms.getClientId(properties, platform));
			if (tokenInfo.getUserInfo() != null) {
				params.add("userId", tokenInfo.getUserInfo().getUserId());
			}
			JSONObject post = Rests.post(properties.getRoleListUrl(), params);
			if (post.get("data") != null) {
				tokenInfo.getUserInfo().setRoles((List<String>) post.get("data"));
			} else {
				tokenInfo.getUserInfo().setRoles(new ArrayList<>());
			}

			// 如果是管理员角色，则企业编码设置成admin
			if (tokenInfo.getUserInfo().getRoles().contains("admin")) {
				tokenInfo.getUserInfo().setOrgNo("admin");
			}

			// token存入redis中
			redisTemplate.opsForValue().set(tokenInfo.getJti(), tokenInfo, properties.getTokenStoreTime(), TimeUnit.SECONDS);

			return Mono.just(Result.build(Status.SUCCESS, "Login successfully", tokenInfo));
		} catch (Exception e) {
			return Mono.just(Result.build(Status.FAILURE, "Error occurred during login"));
		}
	}

	/**
	 * 登录之后若有多个企业海关编码，需要对海关编码进行选择
	 * refreshToken获取token时，需要设置已选择的海关编码
	 *
	 * @param orgNo
	 * @param jti
	 * @return
	 */
	@RequestMapping(value = "/selectOrgNo", method = RequestMethod.GET)
	public Mono<Result> selectOrgNo(@NotBlank @RequestParam(name = "orgNo") String orgNo,
	                                @NotBlank @RequestParam(name = "jti") String jti) {
		Object obj = redisTemplate.opsForValue().get(jti);
		if (obj == null) {
			return Mono.just(Result.build(Status.FAILURE, "Error occurred during selectOrgNo"));
		}
		JwtAccessTokenInfo tokenInfo = (JwtAccessTokenInfo) obj;
		tokenInfo.getUserInfo().setOrgNo(orgNo);
		redisTemplate.opsForValue().set(tokenInfo.getJti(), tokenInfo, properties.getTokenStoreTime(), TimeUnit.SECONDS);
		return Mono.just(Result.build(Status.SUCCESS, "Success saved OrgNo"));
	}
}