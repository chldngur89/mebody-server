package com.mebody.admin.controller;

import com.mebody.admin.dto.AdminStorageImageDto;
import com.mebody.admin.dto.AdminStorageUploadResponse;
import com.mebody.admin.service.AdminStorageService;
import com.mebody.common.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/storage/images")
public class AdminStorageController {
  private final AdminStorageService adminStorageService;

  public AdminStorageController(AdminStorageService adminStorageService) {
    this.adminStorageService = adminStorageService;
  }

  @GetMapping
  public ApiResponse<List<AdminStorageImageDto>> images(@RequestParam(defaultValue = "characters") String prefix) {
    return ApiResponse.ok(adminStorageService.listImages(prefix));
  }

  @PostMapping
  public ApiResponse<AdminStorageUploadResponse> upload(@RequestParam String path, @RequestParam MultipartFile file) {
    return ApiResponse.ok(adminStorageService.uploadImage(path, file));
  }

  @DeleteMapping
  public ApiResponse<Void> delete(@RequestParam String path) {
    adminStorageService.deleteImage(path);
    return ApiResponse.ok(null, "이미지가 삭제되었습니다.");
  }
}
