package com.torresj.unseenauth.dtos.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {
  private Boolean primary;
  private Boolean verified;
  private Source source;
}
