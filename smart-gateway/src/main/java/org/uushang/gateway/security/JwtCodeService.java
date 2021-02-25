package org.uushang.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import org.uushang.gateway.security.config.JwtOauth2ClientProperties;
import org.uushang.gateway.security.exception.UserRedirectRequiredException;
import org.uushang.gateway.security.util.DefaultServerRedirectStrategy;
import org.uushang.gateway.security.util.RandomValueStringGenerator;
import org.uushang.gateway.util.Platforms;

import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * server端获取授权码，并跳转到指定地址
 *
 * @author pengjunjie
 * @date 2019-08-29
 */
@Service
public class JwtCodeService {

	private DefaultServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	private RandomValueStringGenerator stateKeyGenerator = new RandomValueStringGenerator();

	@Autowired
	private JwtOauth2ClientProperties properties;

	public Mono<Void> redirectUser(UserRedirectRequiredException e, ServerWebExchange webExchange) {
		String redirectUri = e.getRedirectUri();
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(redirectUri);
		Map<String, String> requestParams = e.getRequestParams();

		for (Map.Entry entry : requestParams.entrySet()) {
			builder.queryParam((String) entry.getKey(), entry.getValue());
		}

		if (e.getStateKey() != null) {
			builder.queryParam("state", e.getStateKey());
		}

		URI uri = null;
		try {
			uri = new URI(builder.build().encode().toUriString());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		return this.redirectStrategy.sendRedirect(webExchange, uri);
	}

	public UserRedirectRequiredException getRedirectForAuthorization(String platform) {
		Map<String, String> params = new HashMap<>(4);
		params.put("response_type", "code");
		params.put("client_id", Platforms.getClientId(properties, platform));
		params.put("logout_url", properties.getLogoutUrl());
		params.put("redirect_uri", Platforms.getRedirectUrl(properties, platform));

		UserRedirectRequiredException redirectException =
				new UserRedirectRequiredException(properties.getUserAuthorizationUrl(), params);
		String stateKey = this.stateKeyGenerator.generate();
		redirectException.setStateKey(stateKey);
		redirectException.setStateToPreserve(Platforms.getRedirectUrl(properties, platform));
		return redirectException;
	}
}