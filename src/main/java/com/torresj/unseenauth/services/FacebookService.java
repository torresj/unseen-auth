package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthSocialTokenDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.facebook.FacebookUser;
import com.torresj.unseenauth.dtos.facebook.Picture;
import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.Role;
import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.exceptions.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("FACEBOOK")
@Slf4j
@AllArgsConstructor
public class FacebookService implements AuthSocialLogin {
  private final RestTemplate restTemplate;

  private final UserService userService;

  private final JwtService jwtService;

  @Value("${social.login.facebook.url}")
  private final String facebookUrl;

  @Value("${social.login.facebook.picture.url}")
  private final String facebookPictureUrl;

  @Override
  public LoginResponseDTO signIn(AuthSocialTokenDTO authToken)
      throws InvalidAccessTokenException, SocialAPIException, UserInOtherProviderException,
          NonceAlreadyUsedException {

    // Check if we have a token
    if (authToken.token().isBlank()) throw new InvalidAccessTokenException();

    // Call to Facebook GraphQL
    ResponseEntity<FacebookUser> response =
        restTemplate.exchange(
            facebookUrl + authToken.token(),
            HttpMethod.GET,
            new HttpEntity<String>(new HttpHeaders()),
            FacebookUser.class);

    // Check response from Facebook
    if (response.getStatusCode().value() != 200) throw new SocialAPIException();
    if (response.getBody() == null) throw new InvalidAccessTokenException();

    // Get picture
    Picture picture = getPictureInBetterQuality(authToken.token());

    // Create user
    UserEntity userFromFacebook =
        convertFacebookUserToUserEntity(response.getBody(), picture, authToken.nonce());

    // Get user from DB
    UserEntity userFromDB = null;
    try {
      userFromDB = userService.get(userFromFacebook.getEmail());
    } catch (UserNotFoundException ignored) {
    }

    if (userFromDB == null) {
      log.debug(
          "[FACEBOOK SERVICE] creating a new user from facebook: " + userFromFacebook.getEmail());
      userService.save(userFromFacebook);
    } else {
      if (!AuthProvider.FACEBOOK.equals(userFromDB.getProvider()))
        throw new UserInOtherProviderException();
      log.debug(
          "[FACEBOOK SERVICE] user from facebook already exists: " + userFromFacebook.getEmail());
      if (userFromDB.getNonce() >= authToken.nonce()) throw new NonceAlreadyUsedException();
      userService.save(
          userFromDB.toBuilder()
              .photoUrl(userFromDB.getPhotoUrl())
              .numLogins(userFromDB.getNumLogins() + 1)
              .nonce(authToken.nonce())
              .build());
    }

    // Email
    String email = userFromDB != null ? userFromDB.getEmail() : userFromFacebook.getEmail();

    // generating JWT
    String jwt = jwtService.generateJWT(email, AuthProvider.FACEBOOK, Role.USER);

    log.debug("[FACEBOOK SERVICE] JWT generated = " + jwt);
    return new LoginResponseDTO(jwt, email);
  }

  private Picture getPictureInBetterQuality(String token) throws SocialAPIException {
    ResponseEntity<Picture> response =
        restTemplate.exchange(
            facebookPictureUrl + token,
            HttpMethod.GET,
            new HttpEntity<String>(new HttpHeaders()),
            Picture.class);

    if (response.getStatusCode().value() != 200 || response.getBody() == null) {
      throw new SocialAPIException();
    } else {
      return response.getBody();
    }
  }

  private UserEntity convertFacebookUserToUserEntity(
      FacebookUser facebookUser, Picture picture, long nonce) {
    String email = facebookUser.getEmail();
    String photo = picture.getData().getUrl();
    String name = facebookUser.getName();

    return UserEntity.builder()
        .id(null)
        .name(name)
        .photoUrl(photo)
        .email(email)
        .validated(true)
        .role(Role.USER)
        .provider(AuthProvider.FACEBOOK)
        .password("")
        .nonce(nonce)
        .build();
  }
}
