package org.uushang.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.uushang.gateway.security.config.JwtOauth2ClientProperties;
import org.uushang.gateway.security.util.Rests;
import org.uushang.gateway.util.Platforms;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * server端获取token、刷新token
 *
 * @author pengjunjie
 * @date 2019-08-29
 */
@Service
public class JwtTokenService {

	@Autowired
	private JwtOauth2ClientProperties properties;

	@Nullable
	public String obtainAccessToken(String code, String platform) {

		String clientId = Platforms.getClientId(properties, platform);
		String clientSecret = Platforms.getClientSecret(properties, platform);
		String grantType = "authorization_code";
		List<String> scope = Collections.emptyList();
		String accessTokenUri = properties.getAccessTokenUrl();

		String basicMessage = clientId + ":" + clientSecret;
		String base64BasicMessage;
		try {
			base64BasicMessage = Base64.getEncoder().encodeToString(basicMessage.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("client_id", clientId);
		parameters.add("client_secret", clientSecret);
		parameters.add("code", code);
		parameters.add("scope", StringUtils.collectionToDelimitedString(scope, ","));
		parameters.add("grant_type", grantType);
		parameters.add("redirect_uri", Platforms.getRedirectUrl(properties, platform));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		headers.add("Authorization", "basic " + base64BasicMessage);

		HttpEntity<MultiValueMap<String, String>> formEntity = new HttpEntity<>(parameters, headers);

		return Rests.post(accessTokenUri, formEntity);
	}

	@Nullable
	public String refreshToken(String accessToken) {
		MultiValueMap<String, String> parameters= new LinkedMultiValueMap<>();
		parameters.add("accessToken", accessToken);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());

		HttpEntity<MultiValueMap<String, String>> formEntity = new HttpEntity<>(parameters, headers);

		return Rests.post(properties.getRefreshTokenUrl(), formEntity);
	}
}