package org.uushang.gateway.security.exception;

import java.util.Map;

/**
 * 重定向异常
 * @author liyue
 */
public class UserRedirectRequiredException extends RuntimeException {

    private final String redirectUri;

    private final Map<String, String> requestParams;

    private String stateKey;

    private Object stateToPreserve;

    public UserRedirectRequiredException(String redirectUri, Map<String, String> requestParams) {
        super("A redirect is required to get the users approval");
        this.redirectUri = redirectUri;
        this.requestParams = requestParams;
    }

    public String getRedirectUri() {
        return this.redirectUri;
    }

    public Map<String, String> getRequestParams() {
        return this.requestParams;
    }

    public String getStateKey() {
        return this.stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public Object getStateToPreserve() {
        return this.stateToPreserve;
    }

    public void setStateToPreserve(Object stateToPreserve) {
        this.stateToPreserve = stateToPreserve;
    }
}