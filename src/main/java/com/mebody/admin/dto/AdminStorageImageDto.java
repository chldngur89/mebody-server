package com.mebody.admin.dto;

public record AdminStorageImageDto(
    String name,
    String path,
    String publicUrl,
    String updatedAt,
    Long size
) {
}
