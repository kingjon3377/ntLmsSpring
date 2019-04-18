package com.st.novatech.springlms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:database-config.properties")
@SpringBootApplication
public class SpringlmsApplication {

	public static void main(final String[] args) {
		SpringApplication.run(SpringlmsApplication.class, args);
	}

}
