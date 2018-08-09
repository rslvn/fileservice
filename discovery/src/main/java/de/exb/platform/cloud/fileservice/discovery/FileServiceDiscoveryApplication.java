package de.exb.platform.cloud.fileservice.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableEurekaServer
public class FileServiceDiscoveryApplication 
{
    public static void main( String[] args ){
        SpringApplication.run(FileServiceDiscoveryApplication.class, args);
    }
}
