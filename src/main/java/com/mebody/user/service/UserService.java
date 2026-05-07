package com.mebody.user.service;

import com.mebody.bodybti.dto.BodyBtiResponse;
import com.mebody.common.exception.NotFoundException;
import com.mebody.common.security.CurrentUser;
import com.mebody.common.security.CurrentUserService;
import com.mebody.mission.dto.MissionSummaryResponse;
import com.mebody.mission.service.MissionService;
import com.mebody.user.domain.UserProfile;
import com.mebody.user.dto.MeSummaryResponse;
import com.mebody.user.dto.UpdateMeRequest;
import com.mebody.user.dto.UserProfileDto;
import com.mebody.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final CurrentUserService currentUserService;
  private final UserProfileRepository userProfileRepository;
  private final MissionService missionService;

  public UserService(CurrentUserService currentUserService, UserProfileRepository userProfileRepository, MissionService missionService) {
    this.currentUserService = currentUserService;
    this.userProfileRepository = userProfileRepository;
    this.missionService = missionService;
  }

  @Transactional(readOnly = true)
  public UserProfileDto me() {
    return UserProfileDto.from(requireProfile());
  }

  @Transactional
  public UserProfileDto updateMe(UpdateMeRequest request) {
    UserProfile profile = requireProfile();
    if (request.name() != null) profile.setName(request.name());
    if (request.nickname() != null) profile.setNickname(request.nickname());
    if (request.phone() != null) profile.setPhone(request.phone());
    return UserProfileDto.from(profile);
  }

  @Transactional(readOnly = true)
  public BodyBtiResponse bodyBti() {
    return BodyBtiResponse.from(requireProfile());
  }

  @Transactional(readOnly = true)
  public MeSummaryResponse summary() {
    UserProfile profile = requireProfile();
    MissionSummaryResponse mission = missionService.myMissions();
    return new MeSummaryResponse(
        UserProfileDto.from(profile),
        profile.getBodyBtiCode(),
        profile.getBodyBtiTitle(),
        mission.overallAchievementRate(),
        mission.activeMissionCount(),
        mission.completedMissionCount()
    );
  }

  private UserProfile requireProfile() {
    CurrentUser currentUser = currentUserService.requireCurrentUser();
    return userProfileRepository.findById(currentUser.id())
        .orElseThrow(() -> new NotFoundException("회원 정보를 찾을 수 없습니다."));
  }
}
