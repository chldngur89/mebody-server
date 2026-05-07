package com.mebody.mission.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "missions")
public class Mission {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(name = "target_count", nullable = false)
  private int targetCount = 1;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  public UUID getId() { return id; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public int getTargetCount() { return targetCount; }
  public boolean isActive() { return active; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
