package org.uushang.gateway.security.token;

import java.io.Serializable;

/**
 * token info
 *
 * @author pengjunjie
 * @date 2019-08-27
 */
public class JwtAccessTokenInfo implements Serializable {

	private String jti;

	private String globalUserId;

	private Long expireTime;

	private String originTokenValue;

	private JwtClientUserInfo userInfo;

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public String getGlobalUserId() {
		return globalUserId;
	}

	public void setGlobalUserId(String globalUserId) {
		this.globalUserId = globalUserId;
	}

	public Long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

	public JwtClientUserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(JwtClientUserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public String getOriginTokenValue() {
		return originTokenValue;
	}

	public void setOriginTokenValue(String originTokenValue) {
		this.originTokenValue = originTokenValue;
	}
}