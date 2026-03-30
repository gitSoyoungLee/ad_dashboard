package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
