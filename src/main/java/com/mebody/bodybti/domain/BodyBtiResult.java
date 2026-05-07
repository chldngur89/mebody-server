package com.mebody.bodybti.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "body_bti_results")
public class BodyBtiResult {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(nullable = false)
  private String code;

  private String title;
  private String description;

  @Column(name = "score_json", columnDefinition = "jsonb")
  private String scoreJson;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public String getCode() { return code; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public String getScoreJson() { return scoreJson; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
