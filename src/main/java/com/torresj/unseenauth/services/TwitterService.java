package com.torresj.unseenauth.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseenauth.dtos.AuthSocialTokenDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.twitter.TwitterAccessToken;
import com.torresj.unseenauth.exceptions.InvalidAccessTokenException;
import com.torresj.unseenauth.exceptions.NonceAlreadyUsedException;
import com.torresj.unseenauth.exceptions.SocialAPIException;
import com.torresj.unseenauth.exceptions.UserInOtherProviderException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service("TWITTER")
@Slf4j
@AllArgsConstructor
public class TwitterService implements AuthSocialLogin {

  private final RestTemplate restTemplate;

  private final UserService userService;

  private final JwtService jwtService;

  @Value("${social.login.twitter.consumerKey}")
  private final String consumerKey;

  @Value("${social.login.twitter.consumerSecret}")
  private final String consumerSecret;

  @Override
  public LoginResponseDTO signIn(AuthSocialTokenDTO authToken)
      throws InvalidAccessTokenException, SocialAPIException, UserInOtherProviderException,
          NonceAlreadyUsedException {

    // Check if we have a token
    if (authToken.token().isBlank()) throw new InvalidAccessTokenException();

    // Get access token data from request
    byte[] twitterAccessTokenJson = Base64.getDecoder().decode(authToken.token());
    TwitterAccessToken twitterAccessToken = null;
    try {
      twitterAccessToken =
          new ObjectMapper()
              .readValue(
                  new String(twitterAccessTokenJson, StandardCharsets.UTF_8),
                  TwitterAccessToken.class);
    } catch (JsonProcessingException e) {
      log.error("[TWITTER SERVICE] Error parsing Twitter Access Token from request");
      throw new SocialAPIException();
    }

    // Get data from twitter
    TwitterTemplate twitterTemplate =
        new TwitterTemplate(
            consumerKey,
            consumerSecret,
            twitterAccessToken.getToken(),
            twitterAccessToken.getSecret());

    TwitterProfile twitterProfile = twitterTemplate.userOperations().getUserProfile();


    return null;
  }
}
