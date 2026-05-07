package com.mebody.web.dto;

public record PublicConfigResponse(
    String supabaseUrl,
    String supabaseAnonKey,
    String appUrl
) {
}
