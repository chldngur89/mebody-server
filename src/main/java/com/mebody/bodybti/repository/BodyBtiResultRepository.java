package com.mebody.bodybti.repository;

import com.mebody.bodybti.domain.BodyBtiResult;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyBtiResultRepository extends JpaRepository<BodyBtiResult, UUID> {
  List<BodyBtiResult> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
}
