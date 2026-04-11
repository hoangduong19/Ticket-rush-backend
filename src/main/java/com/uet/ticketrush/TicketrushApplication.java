package com.uet.ticketrush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketrushApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketrushApplication.class, args);
	}

}
