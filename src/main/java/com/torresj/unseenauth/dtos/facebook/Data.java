package com.torresj.unseenauth.dtos.facebook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
  private String url;
}
