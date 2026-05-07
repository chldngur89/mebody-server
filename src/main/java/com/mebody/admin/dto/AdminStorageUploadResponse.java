package com.mebody.admin.dto;

public record AdminStorageUploadResponse(
    String path,
    String publicUrl
) {
}
