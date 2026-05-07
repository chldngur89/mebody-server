package com.mebody.user.controller;

import com.mebody.bodybti.dto.BodyBtiResponse;
import com.mebody.common.response.ApiResponse;
import com.mebody.mission.dto.MissionSummaryResponse;
import com.mebody.mission.service.MissionService;
import com.mebody.user.dto.MeSummaryResponse;
import com.mebody.user.dto.UpdateMeRequest;
import com.mebody.user.dto.UserProfileDto;
import com.mebody.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class UserController {
  private final UserService userService;
  private final MissionService missionService;

  public UserController(UserService userService, MissionService missionService) {
    this.userService = userService;
    this.missionService = missionService;
  }

  @GetMapping
  public ApiResponse<UserProfileDto> me() {
    return ApiResponse.ok(userService.me());
  }

  @PatchMapping
  public ApiResponse<UserProfileDto> updateMe(@Valid @RequestBody UpdateMeRequest request) {
    return ApiResponse.ok(userService.updateMe(request));
  }

  @GetMapping("/body-bti")
  public ApiResponse<BodyBtiResponse> bodyBti() {
    return ApiResponse.ok(userService.bodyBti());
  }

  @GetMapping("/missions")
  public ApiResponse<MissionSummaryResponse> missions() {
    return ApiResponse.ok(missionService.myMissions());
  }

  @GetMapping("/summary")
  public ApiResponse<MeSummaryResponse> summary() {
    return ApiResponse.ok(userService.summary());
  }
}
