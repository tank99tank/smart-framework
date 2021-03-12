package org.uushang.smart;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class SmartEurekaServer {
	public static void main(String[] args) {
		new SpringApplicationBuilder(SmartEurekaServer.class).run(args);
	}
}