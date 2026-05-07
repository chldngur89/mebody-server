package com.mebody.bodybti.dto;

import com.mebody.user.domain.UserProfile;

public record BodyBtiResponse(
    String code,
    String title,
    String description
) {
  public static BodyBtiResponse from(UserProfile profile) {
    return new BodyBtiResponse(profile.getBodyBtiCode(), profile.getBodyBtiTitle(), profile.getBodyBtiDescription());
  }
}
