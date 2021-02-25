package org.uushang.gateway.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * client端，全局属性配置类
 *
 * @author pengjunjie
 * @date 2019-08-29
 */
@Configuration
@ConfigurationProperties(prefix = "security.oauth2.client")
@RefreshScope
@Getter
@Setter
public class JwtOauth2ClientProperties {

	private List<String> clientId;

	private List<String> clientSecret;

	private List<String> clientUrl;

	private String userAuthorizationUrl;

	private String accessTokenUrl;

	private String logoutUrl;

	private String ssoLogoutUrl;

	private String refreshTokenUrl;

	private String redirectUrl;

	private String menuTreeUrl;

	private String permissionsUrl;

	@Value("#{'${security.oauth2.url.serverUrl}'.concat('/server/getClientRoleList')}")
	private String roleListUrl;

	private String signKey;

	private Integer tokenStoreTime;

	@Value("#{'${security.oauth2.client.allowUrl}'.split(',')}")
	private List<String> allowUrl;
}