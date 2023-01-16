package com.torresj.unseenauth.dtos.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailAddress {
  private String value;
  private String type;
  private String formattedType;
  private String displayName;
  private Metadata metadata;
}
