package com.mebody.mission.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_mission_progress")
public class UserMissionProgress {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "mission_id")
  private UUID missionId;

  @Column(name = "current_count", nullable = false)
  private int currentCount;

  @Column(name = "target_count", nullable = false)
  private int targetCount;

  @Column(name = "achievement_rate", nullable = false)
  private BigDecimal achievementRate = BigDecimal.ZERO;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime updatedAt;

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public UUID getMissionId() { return missionId; }
  public int getCurrentCount() { return currentCount; }
  public int getTargetCount() { return targetCount; }
  public BigDecimal getAchievementRate() { return achievementRate; }
  public OffsetDateTime getCompletedAt() { return completedAt; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
