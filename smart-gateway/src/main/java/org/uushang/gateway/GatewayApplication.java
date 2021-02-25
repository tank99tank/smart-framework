package org.uushang.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;


/**
 *
 * 启动类
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-02 13:04:14
 * @copyright www.liderong.cn
 */
@SpringCloudApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}