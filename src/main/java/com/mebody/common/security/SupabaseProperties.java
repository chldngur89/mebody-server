package com.mebody.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mebody.supabase")
public record SupabaseProperties(
    String url,
    String anonKey,
    String jwtSecret,
    String jwksUrl,
    String serviceRoleKey
) {
}
