package com.uet.ticketrush.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    // Spring sẽ tự tìm đến đường dẫn cloudinary -> url trong file .yml
    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        // Cloudinary SDK cực kỳ thông minh, nó tự "mổ" cái URL này ra
        // để lấy Key, Secret và Cloud Name mà bạn không cần làm gì thêm.
        return new Cloudinary(cloudinaryUrl);
    }
}