package com.torresj.unseenauth.dtos.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Name {
  private String displayName;
  private String displayNameLastFirst;
  private String unstructuredName;
  private String familyName;
  private String givenName;
  private String middleName;
  private Metadata metadata;
}
