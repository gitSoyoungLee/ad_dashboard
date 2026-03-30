package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {

}
