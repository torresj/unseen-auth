package com.torresj.unseenauth.dtos.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Photo {
  private String url;

  @JsonProperty("default")
  private Boolean df;

  private Metadata metadata;
}
