package com.mebody.mission.repository;

import com.mebody.mission.domain.Mission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, UUID> {
  List<Mission> findByActiveTrueOrderByCreatedAtDesc();
}
