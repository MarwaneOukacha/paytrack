

package com.paytrack.paytrackreigstry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class PaytrackReigstryApplication {
	public static void main(String[] args) {
		SpringApplication.run(PaytrackReigstryApplication.class, args);
	}
}
