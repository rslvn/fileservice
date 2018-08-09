package de.exb.platform.cloud.fileservice.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * File storage configurator
 * 
 * @author resulav
 *
 */
@ConfigurationProperties("storage")
@Getter
@Setter
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location;

}
