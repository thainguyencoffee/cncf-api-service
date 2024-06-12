package com.nguyent.cncfapiservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cncf-api-service.cloudinary")
public record CloudinaryProperties(
        String cloudName,
        String apiKey,
        String apiSecret
) {
}
