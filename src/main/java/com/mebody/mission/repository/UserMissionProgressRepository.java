package com.mebody.mission.repository;

import com.mebody.mission.domain.UserMissionProgress;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMissionProgressRepository extends JpaRepository<UserMissionProgress, UUID> {
  List<UserMissionProgress> findByUserId(UUID userId);
  long countByUserIdAndCompletedAtIsNotNull(UUID userId);
  long countByUserIdAndCompletedAtIsNull(UUID userId);
}
