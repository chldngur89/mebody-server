package com.mebody.user.repository;

import com.mebody.user.domain.UserGrade;
import com.mebody.user.domain.UserProfile;
import com.mebody.user.domain.UserRole;
import com.mebody.user.domain.UserStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>, JpaSpecificationExecutor<UserProfile> {
  Optional<UserProfile> findByAuthUserId(UUID authUserId);
  Optional<UserProfile> findByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCase(String email);
  long countByStatus(UserStatus status);
  long countByRole(UserRole role);
  long countByCreatedAtAfter(OffsetDateTime after);
  Page<UserProfile> findByStatusNot(UserStatus status, Pageable pageable);

  @Query("select coalesce(avg(u.missionAchievementRate), 0) from UserProfile u where u.status <> com.mebody.user.domain.UserStatus.DELETED")
  java.math.BigDecimal averageMissionAchievementRate();
}
