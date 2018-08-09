package de.exb.platform.cloud.fileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import de.exb.platform.cloud.fileservice.properties.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
@EnableEurekaClient
@EnableWebMvc
public class FileServiceApplication {

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		SpringApplication.run(FileServiceApplication.class, args);
	}
}
