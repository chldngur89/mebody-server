package com.mebody.auth.dto;

import java.util.UUID;

public record PublicSignupResponse(
    UUID authUserId,
    String email,
    String displayName
) {
}
