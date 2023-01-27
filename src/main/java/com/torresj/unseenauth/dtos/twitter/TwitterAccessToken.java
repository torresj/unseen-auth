package com.torresj.unseenauth.dtos.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterAccessToken {
	private String token;
	private String secret;
}
