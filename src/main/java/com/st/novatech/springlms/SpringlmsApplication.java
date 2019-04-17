package com.st.novatech.springlms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Main driver class for the application for running from the command line.
 */
@PropertySource("classpath:database-config.properties")
@SpringBootApplication
public class SpringlmsApplication {

	/**
	 * Main method.
	 * @param args parsed by Spring
	 */
	public static void main(final String[] args) {
		SpringApplication.run(SpringlmsApplication.class, args);
	}

}
