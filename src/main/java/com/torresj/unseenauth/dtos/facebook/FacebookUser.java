package com.torresj.unseenauth.dtos.facebook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookUser {
  private String name;
  private String email;
  private Picture picture;
}