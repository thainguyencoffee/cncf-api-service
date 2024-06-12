package com.nguyent.cncfapiservice.cloudinary;

import com.cloudinary.Cloudinary;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class CloudinaryUtils {

    public static String uploadFile(MultipartFile file, Cloudinary cloudinary) throws IOException {
        String url = cloudinary.uploader()
                .upload(file.getBytes(), Map.of("public_id", UUID.randomUUID().toString()))
                .get("url").toString();
        return url.substring(0, url.lastIndexOf("."));
    }

    public static String deleteFile(String publicId, Cloudinary cloudinary) throws IOException {
        return cloudinary.uploader()
                .destroy(publicId, Map.of("invalidate", true))
                .get("result")
                .toString();
    }

    // write a method to split url
    public static String convertUrlToPublicId(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

}
