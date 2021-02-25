package org.uushang.gateway.config.annotation;

import lombok.Getter;

/**
 *
 * 参数名
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-10-10 16:33:37
 * @copyright www.liderong.cn
 */
@Getter
public enum Param {

    /**
     * 来源系统
     */
    PLATFORM("platform"),
    /**
     * token
     */
    ACCESSTOKEN("accessToken"),
    /**
     * 身份ID
     */
    IDENTITYID("identityID"),
    /**
     * 账号ID
     */
    USERID("userId"),
    /**
     * 账号编号
     */
    USERNO("userNo"),
    /**
     * 用户名
     */
    USERNAME("userName");

    String name;

    Param(String name) {
        this.name = name;
    }
}