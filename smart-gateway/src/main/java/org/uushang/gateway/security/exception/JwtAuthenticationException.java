package org.uushang.gateway.security.exception;

/**
 * 认证异常
 * @author liyue
 */
public class JwtAuthenticationException extends RuntimeException {

	public JwtAuthenticationException(String msg) {
		super(msg);
	}
}
