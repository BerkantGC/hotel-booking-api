package com.hotelbooking.notification_service.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class GetHeaders {
    public static HttpHeaders getHeaders(String internalSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Secret", internalSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
