package io.soyoung.addashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdDashboardApplication.class, args);
	}

}
