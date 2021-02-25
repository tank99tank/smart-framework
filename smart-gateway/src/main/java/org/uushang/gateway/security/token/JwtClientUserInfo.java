package org.uushang.gateway.security.token;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 返给客户端的用户信息
 *
 * @author pengjunjie
 * @date 2019-08-26
 */
@ToString
@Getter
@Setter
public class JwtClientUserInfo implements Serializable {

	private String userId;

	private String userNo;

	private String orgNo;

	private String orgName;

	private String orgCreditCode;

	private String username;

	private String password;

	private String nickname;

	private String email;

	private String telephone;

	private String address;

	private String fullPath;

	private String cardNumber;

	private List<String> roles;
}