package com.mebody.admin.dto;

import com.mebody.user.domain.UserGrade;
import com.mebody.user.domain.UserRole;
import com.mebody.user.domain.UserStatus;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AdminUpdateUserRequest(
    @Size(max = 80) String name,
    @Size(max = 80) String nickname,
    @Size(max = 40) String phone,
    UserRole role,
    UserStatus status,
    UserGrade grade,
    @Size(max = 20) String bodyBtiCode,
    @Size(max = 120) String bodyBtiTitle,
    String bodyBtiDescription,
    BigDecimal missionAchievementRate
) {
}
