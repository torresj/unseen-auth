package com.torresj.unseenauth.dtos.facebook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Picture {
  private com.torresj.unseenauth.dtos.facebook.Data data;
}
