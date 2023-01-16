package com.torresj.unseenauth.dtos.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class People {
  private String resourceName;
  private String etag;
  private List<Name> names;
  private List<Photo> photos;
  private List<EmailAddress> emailAddresses;
}
