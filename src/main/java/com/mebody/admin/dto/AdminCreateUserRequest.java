package com.mebody.admin.dto;

import com.mebody.user.domain.UserGrade;
import com.mebody.user.domain.UserRole;
import com.mebody.user.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
    @Email @NotBlank String email,
    @Size(max = 80) String name,
    @Size(max = 80) String nickname,
    @Size(max = 40) String phone,
    UserRole role,
    UserStatus status,
    UserGrade grade
) {
}
