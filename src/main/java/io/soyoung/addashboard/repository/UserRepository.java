package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.User;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
