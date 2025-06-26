package com.hotelbooking.hotel_admin_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.hotelbooking.common_model") // <== Burası kritik
public class HotelAdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelAdminServiceApplication.class, args);
	}

}
