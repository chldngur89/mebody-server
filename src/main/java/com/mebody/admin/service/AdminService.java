package com.mebody.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mebody.admin.domain.AdminAuditLog;
import com.mebody.admin.dto.AdminCreateUserRequest;
import com.mebody.admin.dto.AdminDashboardSummary;
import com.mebody.admin.dto.AdminUpdateUserRequest;
import com.mebody.admin.dto.AdminUserSearchRequest;
import com.mebody.admin.repository.AdminAuditLogRepository;
import com.mebody.common.exception.NotFoundException;
import com.mebody.common.exception.ApiException;
import com.mebody.common.security.CurrentUser;
import com.mebody.common.security.CurrentUserService;
import com.mebody.user.domain.UserGrade;
import com.mebody.user.domain.UserProfile;
import com.mebody.user.domain.UserRole;
import com.mebody.user.domain.UserStatus;
import com.mebody.user.dto.UserProfileDto;
import com.mebody.user.repository.UserProfileRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

@Service
public class AdminService {
  private final CurrentUserService currentUserService;
  private final UserProfileRepository userProfileRepository;
  private final AdminAuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper;
  private final JdbcTemplate jdbcTemplate;

  public AdminService(CurrentUserService currentUserService, UserProfileRepository userProfileRepository, AdminAuditLogRepository auditLogRepository, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
    this.currentUserService = currentUserService;
    this.userProfileRepository = userProfileRepository;
    this.auditLogRepository = auditLogRepository;
    this.objectMapper = objectMapper;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Transactional(readOnly = true)
  public AdminDashboardSummary dashboardSummary() {
    requireAdmin();
    OffsetDateTime todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().atOffset(ZoneOffset.UTC);
    return new AdminDashboardSummary(
        userProfileRepository.count(),
        userProfileRepository.countByStatus(UserStatus.ACTIVE),
        userProfileRepository.countByStatus(UserStatus.SUSPENDED),
        userProfileRepository.countByRole(UserRole.SELLER),
        userProfileRepository.countByRole(UserRole.ADMIN),
        userProfileRepository.countByCreatedAtAfter(todayStart),
        defaultZero(userProfileRepository.averageMissionAchievementRate())
    );
  }

  @Transactional(readOnly = true)
  public UserProfileDto adminMe() {
    CurrentUser admin = requireAdmin();
    return toDtoWithLatestResult(findUser(admin.id()));
  }

  @Transactional(readOnly = true)
  public Page<UserProfileDto> users(AdminUserSearchRequest request) {
    requireAdmin();
    Pageable pageable = PageRequest.of(
        Math.max(request.page(), 0),
        Math.min(Math.max(request.size(), 1), 100),
        Sort.by(Sort.Direction.DESC, "createdAt")
    );
    return userProfileRepository.findAll(specification(request), pageable).map(this::toDtoWithLatestResult);
  }

  @Transactional(readOnly = true)
  public UserProfileDto user(UUID id) {
    requireAdmin();
    return toDtoWithLatestResult(findUser(id));
  }

  @Transactional
  public UserProfileDto createUser(AdminCreateUserRequest request) {
    requireAdmin();
    throw new ApiException(
        HttpStatus.NOT_IMPLEMENTED,
        "관리자 수동 회원 생성은 Supabase Auth service role 연동 후 활성화합니다. 현재는 회원가입 화면에서 Supabase Auth로 생성해주세요."
    );
  }

  @Transactional
  public UserProfileDto updateUser(UUID id, AdminUpdateUserRequest request) {
    CurrentUser admin = requireAdmin();
    UserProfile profile = findUser(id);
    String before = toJson(profile);

    if (request.name() != null) {
      profile.setName(request.name());
      profile.setDisplayName(request.name());
    }
    if (request.nickname() != null) profile.setNickname(request.nickname());
    if (request.phone() != null) profile.setPhone(request.phone());
    if (request.role() != null) profile.setRole(request.role());
    if (request.status() != null) profile.setStatus(request.status());
    if (request.grade() != null) profile.setGrade(request.grade());
    if (request.bodyBtiCode() != null) profile.setBodyBtiCode(request.bodyBtiCode());
    if (request.bodyBtiTitle() != null) profile.setBodyBtiTitle(request.bodyBtiTitle());
    if (request.bodyBtiDescription() != null) profile.setBodyBtiDescription(request.bodyBtiDescription());
    if (request.missionAchievementRate() != null) profile.setMissionAchievementRate(request.missionAchievementRate());

    auditRaw(admin, "UPDATE_USER", profile.getId(), before, toJson(profile));
    return toDtoWithLatestResult(profile);
  }

  @Transactional
  public void deleteUser(UUID id) {
    CurrentUser admin = requireAdmin();
    UserProfile profile = findUser(id);
    String before = toJson(profile);
    profile.setStatus(UserStatus.DELETED);
    profile.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
    auditRaw(admin, "SOFT_DELETE_USER", profile.getId(), before, toJson(profile));
  }

  private CurrentUser requireAdmin() {
    return currentUserService.requireRole(UserRole.ADMIN);
  }

  private UserProfile findUser(UUID id) {
    return userProfileRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
  }

  private UserProfileDto toDtoWithLatestResult(UserProfile profile) {
    LatestResultSnapshot latest = findLatestResult(profile.getId());
    if (latest == null && profile.getAuthUserId() != null && !profile.getAuthUserId().equals(profile.getId())) {
      latest = findLatestResult(profile.getAuthUserId());
    }
    if (latest == null) return UserProfileDto.from(profile);
    return UserProfileDto.from(profile, latest.id(), latest.code(), latest.completedAt());
  }

  private LatestResultSnapshot findLatestResult(UUID userId) {
    if (userId == null) return null;

    try {
      return jdbcTemplate.query(
          """
          select id, calculated_code, completed_at
          from public.questionnaire_responses
          where user_id = ?
            and status = 'completed'
            and calculated_code is not null
          order by completed_at desc nulls last, updated_at desc nulls last, created_at desc nulls last
          limit 1
          """,
          ps -> ps.setObject(1, userId),
          rs -> {
            if (!rs.next()) return null;
            return new LatestResultSnapshot(
                rs.getObject("id", UUID.class),
                rs.getString("calculated_code"),
                rs.getObject("completed_at", OffsetDateTime.class)
            );
          }
      );
    } catch (DataAccessException ignored) {
      return null;
    }
  }

  private Specification<UserProfile> specification(AdminUserSearchRequest request) {
    return (root, query, cb) -> {
      var predicates = new ArrayList<Predicate>();
      if (!request.includeDeleted()) {
        predicates.add(cb.notEqual(root.get("status"), UserStatus.DELETED));
      }
      if (request.role() != null) predicates.add(cb.equal(root.get("role"), request.role()));
      if (request.status() != null) predicates.add(cb.equal(root.get("status"), request.status()));
      if (request.grade() != null) predicates.add(cb.equal(root.get("grade"), request.grade()));
      if (request.search() != null && !request.search().isBlank()) {
        String pattern = "%" + request.search().toLowerCase() + "%";
        predicates.add(cb.or(
            cb.like(cb.lower(root.get("email")), pattern),
            cb.like(cb.lower(root.get("name")), pattern),
            cb.like(cb.lower(root.get("nickname")), pattern)
        ));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  private void auditRaw(CurrentUser admin, String action, UUID targetId, String beforeJson, String afterJson) {
    auditLogRepository.save(new AdminAuditLog(admin.id(), action, "USER", targetId, beforeJson, afterJson));
  }

  private String toJson(Object value) {
    if (value == null) return null;
    try {
      return objectMapper.writeValueAsString(value instanceof UserProfile profile ? UserProfileDto.from(profile) : value);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  private BigDecimal defaultZero(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private record LatestResultSnapshot(UUID id, String code, OffsetDateTime completedAt) {}
}
