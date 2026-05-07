package com.mebody.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mebody.auth.dto.PublicSignupRequest;
import com.mebody.auth.dto.PublicSignupResponse;
import com.mebody.common.exception.ApiException;
import com.mebody.common.security.SupabaseProperties;
import com.mebody.user.domain.UserProfile;
import com.mebody.user.repository.UserProfileRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicAuthService {
  private final SupabaseProperties supabaseProperties;
  private final UserProfileRepository userProfileRepository;
  private final ObjectMapper objectMapper;
  private final JdbcTemplate jdbcTemplate;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  public PublicAuthService(SupabaseProperties supabaseProperties, UserProfileRepository userProfileRepository, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
    this.supabaseProperties = supabaseProperties;
    this.userProfileRepository = userProfileRepository;
    this.objectMapper = objectMapper;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Transactional
  public PublicSignupResponse signup(PublicSignupRequest request) {
    AuthUser authUser = createOrFindConfirmedAuthUser(request);
    UUID authUserId = authUser.id();
    String email = authUser.email().trim().toLowerCase();
    String displayName = normalize(request.displayName());

    UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
        .or(() -> userProfileRepository.findByEmailIgnoreCase(email))
        .orElseGet(() -> UserProfile.newMember(authUserId, email));

    if (profile.getId() == null) profile.setId(authUserId);
    profile.setAuthUserId(authUserId);
    profile.setEmail(email);
    if (displayName != null) {
      profile.setDisplayName(displayName);
      profile.setName(displayName);
    }
    userProfileRepository.save(profile);

    return new PublicSignupResponse(authUserId, email, displayName);
  }

  private AuthUser createOrFindConfirmedAuthUser(PublicSignupRequest request) {
    String serviceRoleKey = supabaseProperties.serviceRoleKey();
    if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SUPABASE_SERVICE_ROLE_KEY가 설정되어 있지 않습니다.");
    }

    String email = request.email().trim().toLowerCase();
    try {
      Map<String, Object> payload = new HashMap<>();
      payload.put("email", email);
      payload.put("password", request.password());
      payload.put("email_confirm", true);
      payload.put("user_metadata", Map.of("display_name", normalize(request.displayName()) == null ? "" : normalize(request.displayName())));

      HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(projectUrl() + "/auth/v1/admin/users"))
          .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
          .header("Authorization", "Bearer " + serviceRoleKey)
          .header("apikey", serviceRoleKey)
          .header("Content-Type", "application/json")
          .build();
      HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JsonNode body = objectMapper.readTree(response.body());

      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        return new AuthUser(UUID.fromString(body.path("id").asText()), body.path("email").asText(email));
      }

      String message = body.path("msg").asText(body.path("message").asText("회원가입 처리에 실패했습니다."));
      if (response.statusCode() == 422 || response.statusCode() == 400 || message.toLowerCase().contains("already")) {
        return findAuthUserByEmail(email)
            .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다. 로그인으로 진행해주세요."));
      }
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Supabase Auth 회원 생성 실패: " + message);
    } catch (ApiException e) {
      throw e;
    } catch (IOException e) {
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Supabase Auth 요청을 처리하지 못했습니다.");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Supabase Auth 요청이 중단되었습니다.");
    }
  }

  private Optional<AuthUser> findAuthUserByEmail(String email) {
    return jdbcTemplate.query(
        "select id, email from auth.users where lower(email) = lower(?) limit 1",
        ps -> ps.setString(1, email),
        rs -> {
          if (!rs.next()) return Optional.empty();
          return Optional.of(new AuthUser(rs.getObject("id", UUID.class), rs.getString("email")));
        }
    );
  }

  private String projectUrl() {
    String url = supabaseProperties.url();
    if (url != null && !url.isBlank()) return url.replaceAll("/+$", "");
    String jwksUrl = supabaseProperties.jwksUrl();
    if (jwksUrl != null && jwksUrl.contains("/auth/")) {
      return jwksUrl.substring(0, jwksUrl.indexOf("/auth/")).replaceAll("/+$", "");
    }
    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SUPABASE_URL 또는 SUPABASE_JWKS_URL 설정이 필요합니다.");
  }

  private String normalize(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  private record AuthUser(UUID id, String email) {
  }
}
