package org.uushang.gateway.util;

import java.util.List;

import org.uushang.gateway.security.config.JwtOauth2ClientProperties;

/**
 *
 * 多个应用下获取对应某个应用的相关信息
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-10-08 10:45:49
 * @copyright www.liderong.cn
 */
public class Platforms {

//    public static final String PLATFORM_PAVE = "PAVE";
//
//    public static final String PLATFORM_BILLING = "BILLING";
//
//    public static final String PLATFORM_UC = "UC";

    public static final String PLATFORM_EWTP = "EWTP";

    public static String getClientId(JwtOauth2ClientProperties properties, String platform) {
        return getClientInfo(properties.getClientId(), platform);
    }

    public static String getClientSecret(JwtOauth2ClientProperties properties, String platform) {
        return getClientInfo(properties.getClientSecret(), platform);
    }

    public static String getClientUrl(JwtOauth2ClientProperties properties, String platform) {
        return getClientInfo(properties.getClientUrl(), platform);
    }

    public static String getRedirectUrl(JwtOauth2ClientProperties properties, String platform) {
        String clientUrl = getClientUrl(properties, platform);
        return clientUrl + properties.getRedirectUrl();
    }

    private static String getClientInfo(List<String> source, String platform) {
//        if (PLATFORM_PAVE.equals(platform)) {
//            return source.get(0);
//        }
//        if (PLATFORM_BILLING.equals(platform)) {
//            return source.get(1);
//        }
//        if (PLATFORM_UC.equals(platform)) {
//            return source.get(2);
//        }
        if (PLATFORM_EWTP.equals(platform)) {
            return source.get(0);
        }
        throw new IllegalArgumentException("Unable to match to platform:" + platform);
    }
}