package com.mebody.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
    @Size(max = 80) String name,
    @Size(max = 80) String nickname,
    @Size(max = 40) String phone
) {
}
