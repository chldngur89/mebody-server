package com.mebody.seller.controller;

import com.mebody.common.response.ApiResponse;
import com.mebody.common.security.CurrentUserService;
import com.mebody.seller.dto.SellerDashboardResponse;
import com.mebody.user.domain.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller")
public class SellerController {
  private final CurrentUserService currentUserService;

  public SellerController(CurrentUserService currentUserService) {
    this.currentUserService = currentUserService;
  }

  @GetMapping("/dashboard")
  public ApiResponse<SellerDashboardResponse> dashboard() {
    currentUserService.requireRole(UserRole.SELLER, UserRole.ADMIN);
    return ApiResponse.ok(SellerDashboardResponse.preparing());
  }
}
