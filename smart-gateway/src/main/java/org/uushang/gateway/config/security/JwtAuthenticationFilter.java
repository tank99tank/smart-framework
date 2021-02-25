package org.uushang.gateway.config.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.owinfo.utils.constants.NumberConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.uushang.gateway.constant.Headers;
import org.uushang.gateway.security.JwtCodeService;
import org.uushang.gateway.security.JwtTokenService;
import org.uushang.gateway.security.config.JwtOauth2ClientProperties;
import org.uushang.gateway.security.exception.JwtAuthenticationException;
import org.uushang.gateway.security.exception.UserRedirectRequiredException;
import org.uushang.gateway.security.token.JwtAccessTokenInfo;
import org.uushang.gateway.security.token.JwtClientUserInfo;
import org.uushang.gateway.security.util.JwtTokenUtils;
import org.uushang.gateway.util.ServerUtil;

import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 对所有的请求进行拦截，判断token
 *
 * @author pengjunjie
 * @date 2019-08-26
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements WebFilter, Ordered {

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private JwtOauth2ClientProperties properties;

	@Autowired
	private JwtTokenService tokenService;

	@Autowired
	private JwtCodeService codeService;

	@Override
	public int getOrder() {
		return -10;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

		//白名单验证
		String url = serverWebExchange.getRequest().getPath().pathWithinApplication().value();
		List<String> allowUrl = properties.getAllowUrl();
		if (allowUrl.contains(url)) {
			return webFilterChain.filter(serverWebExchange);
		}

		//验证请求头
		String authc = serverWebExchange.getRequest().getHeaders().getFirst(Headers.AUTHENTICATION);
		if (StringUtils.isEmpty(authc)) {
			log.warn("received a request, but authentication header is null, url -> {}", url);
			return ServerUtil.write(serverWebExchange.getResponse(),
					HttpStatus.UNAUTHORIZED, "request rejected, verification failed");
		}

		String platform = serverWebExchange.getRequest().getHeaders().getFirst(Headers.PLATFORM);
		if (StringUtils.isEmpty(platform)) {
			log.warn("received a request, but platform header is null, url -> {}", url);
			return ServerUtil.write(serverWebExchange.getResponse(),
					HttpStatus.BAD_REQUEST, "request rejected, unknown source");
		}

		//验证Authentication是否是特定值
        //此处存在页面第一次访问未登录时Authentication为空的问题
        //暂定方案为Authentication携带一个默认值
        if(Headers.AUTHENTICATION_DEFAULT.equals(authc)) {
            //获取授权码重新登录
            UserRedirectRequiredException redirect = codeService.getRedirectForAuthorization(platform);
            return codeService.redirectUser(redirect, serverWebExchange);
        }

		//对前端传入的token进行验签，验签失败，则拒绝访问
		String claims;
		try {
			claims = JwtHelper.decodeAndVerify(authc, new MacSigner(properties.getSignKey())).getClaims();
		} catch (Exception e) {
			log.warn("access denied, illegal request, url -> {}, ex -> {}", url, e.toString());
			return ServerUtil.write(serverWebExchange.getResponse(),
					HttpStatus.FORBIDDEN, "access denied, illegal request");
		}

		//验证通过执行处理
		return processing(serverWebExchange, webFilterChain, claims, platform);
	}

	private Mono<Void> processing(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain,
								  String claims, String platform) {
		//获取本地存储的token进行判断，若不存在则抛出异常、若存在判断是否进行token刷新
		try {
			JSONObject claimsJson = JSON.parseObject(claims);
			String jti = claimsJson.getString("jti");
			Object obj = redisTemplate.opsForValue().get(jti);
			if (obj == null) {
				//登陆过期
				throw new JwtAuthenticationException("Unable to read tokens from redis based on header");
			}
			JwtAccessTokenInfo tokenInfo = (JwtAccessTokenInfo) obj;
			JwtClientUserInfo userInfo = tokenInfo.getUserInfo();

			//如果token过期则刷新token
			Long expireTime = tokenInfo.getExpireTime();
			if (expireTime < (System.currentTimeMillis() / NumberConstant.ONE_THOUSAND)) {
				String refreshToken = tokenService.refreshToken(tokenInfo.getOriginTokenValue());
				Map<String, Object> map = JwtTokenUtils.getTokenInfo(refreshToken, properties.getSignKey());
				JwtAccessTokenInfo refreshTokenInfo = (JwtAccessTokenInfo) map.get("tokenInfo");

				//重新将token存入redis中, 保存已经选择的多个海关编码中的其中一个编码
				if (refreshTokenInfo != null && refreshTokenInfo.getUserInfo() != null) {
					refreshTokenInfo.getUserInfo().setUserNo(userInfo.getUserNo());
					refreshTokenInfo.getUserInfo().setRoles(userInfo.getRoles());
					redisTemplate.opsForValue().set(tokenInfo.getJti(), refreshTokenInfo,
							properties.getTokenStoreTime(), TimeUnit.SECONDS);

					//可能存在用户信息更新的情况 当token刷新之后重新进行一次赋值
					userInfo = refreshTokenInfo.getUserInfo();
				}
			}

			// 每次访问更新token过期时间
			redisTemplate.expire(jti, properties.getTokenStoreTime(), TimeUnit.SECONDS);

			// TODO: 2019/12/6
			//=============================添加处理流程==================================//
			//																		    //
			//				        验证用户是否有访问此请求的权限
			//
			//==========================================================================//

			//将身份信息传递到下游程序
			String info;
			ServerHttpRequest mutateRequest;
			try {
				info = URLEncoder.encode(JSONObject.toJSONString(userInfo), "UTF-8");
				mutateRequest = serverWebExchange.getRequest().mutate().header(Headers.USER_INFORMATION, info).build();
			} catch (UnsupportedEncodingException e) {
				log.error("Failed to code user information, enc -> UTF-8, info ->" + userInfo.toString());
				return ServerUtil.write(serverWebExchange.getResponse(), HttpStatus.INTERNAL_SERVER_ERROR,
						"Failed to code user information, unable to perform subsequent processing");
			}

			return webFilterChain.filter(serverWebExchange.mutate().request(mutateRequest).build());
		} catch (JwtAuthenticationException e) {
			//重新登录
			log.warn(e.getMessage());
			//获取授权码重新登录
			UserRedirectRequiredException redirect = codeService.getRedirectForAuthorization(platform);
			return codeService.redirectUser(redirect, serverWebExchange);
		} catch (Exception e) {
			log.error("An error occurred during the authentication process, ex -> {}", e.toString());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			return ServerUtil.write(serverWebExchange.getResponse(), HttpStatus.INTERNAL_SERVER_ERROR,
					"An error occurred during the authentication process");
		}
	}
}