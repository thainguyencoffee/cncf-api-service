package com.nguyent.cncfapiservice.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties cloudinaryProperties){
        return new Cloudinary(Map.of(
                "cloud_name", cloudinaryProperties.cloudName(),
                "api_key", cloudinaryProperties.apiKey(),
                "api_secret", cloudinaryProperties.apiSecret()
        ));
    }

}
