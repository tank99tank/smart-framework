package org.uushang.gateway.security.exception;

/**
 * 解析异常
 *
 * @author liyue
 */
public class JwtParseException extends RuntimeException {

	public JwtParseException(String msg) {
		super(msg);
	}
}
