package com.mebody.bodybti.service;

import com.mebody.bodybti.dto.BodyBtiResponse;
import com.mebody.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class BodyBtiService {
  private final UserService userService;

  public BodyBtiService(UserService userService) {
    this.userService = userService;
  }

  public BodyBtiResponse myBodyBti() {
    return userService.bodyBti();
  }
}
