package com.mebody.user.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
  @Id
  private UUID id;

  @Column(name = "auth_user_id", unique = true)
  private UUID authUserId;

  @Column(unique = true)
  private String email;

  @Column(name = "display_name")
  private String displayName;

  private String name;
  private String nickname;
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role = UserRole.MEMBER;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status = UserStatus.ACTIVE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserGrade grade = UserGrade.BASIC;

  @Column(name = "body_bti_code")
  private String bodyBtiCode;

  @Column(name = "body_bti_title")
  private String bodyBtiTitle;

  @Column(name = "body_bti_description")
  private String bodyBtiDescription;

  @Column(name = "mission_achievement_rate", nullable = false)
  private BigDecimal missionAchievementRate = BigDecimal.ZERO;

  @Column(name = "marketing_opt_in", nullable = false)
  private boolean marketingOptIn = false;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  public static UserProfile newMember(UUID authUserId, String email) {
    UserProfile profile = new UserProfile();
    profile.id = authUserId;
    profile.authUserId = authUserId;
    profile.email = email;
    profile.role = UserRole.MEMBER;
    profile.status = UserStatus.ACTIVE;
    profile.grade = UserGrade.BASIC;
    profile.missionAchievementRate = BigDecimal.ZERO;
    return profile;
  }

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public UUID getAuthUserId() { return authUserId; }
  public void setAuthUserId(UUID authUserId) { this.authUserId = authUserId; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getNickname() { return nickname; }
  public void setNickname(String nickname) { this.nickname = nickname; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public UserRole getRole() { return role; }
  public void setRole(UserRole role) { this.role = role; }
  public UserStatus getStatus() { return status; }
  public void setStatus(UserStatus status) { this.status = status; }
  public UserGrade getGrade() { return grade; }
  public void setGrade(UserGrade grade) { this.grade = grade; }
  public String getBodyBtiCode() { return bodyBtiCode; }
  public void setBodyBtiCode(String bodyBtiCode) { this.bodyBtiCode = bodyBtiCode; }
  public String getBodyBtiTitle() { return bodyBtiTitle; }
  public void setBodyBtiTitle(String bodyBtiTitle) { this.bodyBtiTitle = bodyBtiTitle; }
  public String getBodyBtiDescription() { return bodyBtiDescription; }
  public void setBodyBtiDescription(String bodyBtiDescription) { this.bodyBtiDescription = bodyBtiDescription; }
  public BigDecimal getMissionAchievementRate() { return missionAchievementRate; }
  public void setMissionAchievementRate(BigDecimal missionAchievementRate) { this.missionAchievementRate = missionAchievementRate; }
  public boolean isMarketingOptIn() { return marketingOptIn; }
  public void setMarketingOptIn(boolean marketingOptIn) { this.marketingOptIn = marketingOptIn; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public OffsetDateTime getDeletedAt() { return deletedAt; }
  public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
