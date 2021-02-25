package org.uushang.gateway.security.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.util.StringUtils;
import org.uushang.gateway.security.token.JwtAccessTokenInfo;
import org.uushang.gateway.security.token.JwtClientUserInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author pengjunjie
 * @date 2019-08-29
 */
public class JwtTokenUtils {

	public static Map<String, Object> getTokenInfo(String accessTokenValue, String signKey) {
		Map<String, Object> map = new HashMap<>(2);
		JSONObject json = JSON.parseObject(accessTokenValue);

		MacSigner macSigner = new MacSigner(signKey);
		// tokenValue
		String signedTokenValue = json.getString("access_token");
		String claims = JwtHelper.decodeAndVerify(signedTokenValue, macSigner).getClaims();
		JSONObject claimsJson = JSON.parseObject(claims);

		JwtAccessTokenInfo tokenInfo = new JwtAccessTokenInfo();
		String jti = claimsJson.getString("jti");
		String globalUserId = claimsJson.getString("globalUserId");
		Long exp = claimsJson.getLong("exp");
		tokenInfo.setJti(jti);
		tokenInfo.setGlobalUserId(globalUserId);
		tokenInfo.setExpireTime(exp);
		tokenInfo.setOriginTokenValue(signedTokenValue);

		String userInfo = claimsJson.getString("userInfo");
		if (!StringUtils.isEmpty(userInfo)) {
			JwtClientUserInfo jwtClientUserInfo = JSON.parseObject(userInfo, JwtClientUserInfo.class);
			tokenInfo.setUserInfo(jwtClientUserInfo);
		}

		map.put("tokenInfo", tokenInfo);
		map.put("claimsJson", claimsJson);
		return map;
	}
}