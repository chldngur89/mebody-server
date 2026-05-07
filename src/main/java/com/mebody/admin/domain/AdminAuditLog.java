package com.mebody.admin.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "admin_audit_logs")
public class AdminAuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "admin_user_id")
  private UUID adminUserId;

  @Column(nullable = false)
  private String action;

  @Column(name = "target_type")
  private String targetType;

  @Column(name = "target_id")
  private UUID targetId;

  @Column(name = "before_json", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String beforeJson;

  @Column(name = "after_json", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String afterJson;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected AdminAuditLog() {}

  public AdminAuditLog(UUID adminUserId, String action, String targetType, UUID targetId, String beforeJson, String afterJson) {
    this.adminUserId = adminUserId;
    this.action = action;
    this.targetType = targetType;
    this.targetId = targetId;
    this.beforeJson = beforeJson;
    this.afterJson = afterJson;
  }

  public UUID getId() { return id; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
