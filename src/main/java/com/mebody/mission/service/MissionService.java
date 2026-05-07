package com.mebody.mission.service;

import com.mebody.common.security.CurrentUser;
import com.mebody.common.security.CurrentUserService;
import com.mebody.mission.dto.MissionProgressDto;
import com.mebody.mission.dto.MissionSummaryResponse;
import com.mebody.mission.repository.UserMissionProgressRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionService {
  private final CurrentUserService currentUserService;
  private final UserMissionProgressRepository progressRepository;

  public MissionService(CurrentUserService currentUserService, UserMissionProgressRepository progressRepository) {
    this.currentUserService = currentUserService;
    this.progressRepository = progressRepository;
  }

  @Transactional(readOnly = true)
  public MissionSummaryResponse myMissions() {
    CurrentUser user = currentUserService.requireCurrentUser();
    var progress = progressRepository.findByUserId(user.id());
    BigDecimal average = progress.isEmpty()
        ? BigDecimal.ZERO
        : progress.stream()
            .map(p -> p.getAchievementRate() == null ? BigDecimal.ZERO : p.getAchievementRate())
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(progress.size()), 2, RoundingMode.HALF_UP);
    long completed = progress.stream().filter(p -> p.getCompletedAt() != null).count();
    long active = progress.size() - completed;
    List<MissionProgressDto> items = progress.stream().map(MissionProgressDto::from).toList();
    return new MissionSummaryResponse(average, active, completed, items);
  }
}
