package com.hotelbooking.notification_service.repository;

import com.hotelbooking.common_model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
