package com.mebody.auth.controller;

import com.mebody.auth.dto.PublicSignupRequest;
import com.mebody.auth.dto.PublicSignupResponse;
import com.mebody.auth.service.PublicAuthService;
import com.mebody.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth")
public class PublicAuthController {
  private final PublicAuthService publicAuthService;

  public PublicAuthController(PublicAuthService publicAuthService) {
    this.publicAuthService = publicAuthService;
  }

  @PostMapping("/signup")
  public ApiResponse<PublicSignupResponse> signup(@Valid @RequestBody PublicSignupRequest request) {
    return ApiResponse.ok(publicAuthService.signup(request));
  }
}
