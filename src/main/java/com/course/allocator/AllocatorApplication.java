package com.course.allocator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AllocatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AllocatorApplication.class, args);
	}

}
