package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByUtmCampaign(String utmCampaign);

    @Query("SELECT CAST(u.createdAt AS LocalDate), COUNT(u) FROM User u "
        + "WHERE u.createdAt BETWEEN :start AND :end "
        + "GROUP BY CAST(u.createdAt AS LocalDate) "
        + "ORDER BY CAST(u.createdAt AS LocalDate)")
    List<Object[]> countDailyUsersBetween(@Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}
