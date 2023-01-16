package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthSocialTokenDTO;
import com.torresj.unseenauth.dtos.google.EmailAddress;
import com.torresj.unseenauth.dtos.google.Name;
import com.torresj.unseenauth.dtos.google.People;
import com.torresj.unseenauth.dtos.google.Photo;
import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.Role;
import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleService implements AuthSocialLogin {
  private static final String GOOGLE_URL =
      "https://content-people.googleapis.com/v1/people/me?personFields=names,emailAddresses,photos";

  private final RestTemplate restTemplate;

  private final UserService userService;

  private final JwtService jwtService;

  @Override
  public String signIn(AuthSocialTokenDTO authToken)
      throws InvalidAccessTokenException, SocialAPIException, UserInOtherProviderException,
          NonceAlreadyUsedException {

    // Check if we have a token
    if (authToken.token().isBlank()) throw new InvalidAccessTokenException();

    // Call to google people API v1
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + authToken.token());
    ResponseEntity<People> response =
        restTemplate.exchange(
            GOOGLE_URL, HttpMethod.GET, new HttpEntity<String>(headers), People.class);

    // Check response from google
    if (response == null || response.getStatusCode().value() != 200) throw new SocialAPIException();
    if (response.getBody() == null) throw new InvalidAccessTokenException();

    // Create user
    UserEntity userFromGoogle = convertPeopleToUser(response.getBody(), authToken.nonce());

    // Get user from DB
    UserEntity userFromDB = null;
    try {
      userFromDB = userService.get(userFromGoogle.getEmail());
    } catch (UserNotFoundException exception) {
    }

    if (userFromDB == null) {
      log.debug("[GOOGLE SERVICE] creating a new user from google: " + userFromGoogle.getEmail());
      userService.save(userFromGoogle);
    } else {
      if (!AuthProvider.GOOGLE.equals(userFromDB.getProvider()))
        throw new UserInOtherProviderException();
      log.debug("[GOOGLE SERVICE] user from google already exists: " + userFromGoogle.getEmail());
      if (userFromDB.getNonce() >= authToken.nonce()) throw new NonceAlreadyUsedException();
      userService.save(
          userFromDB.toBuilder()
              .photoUrl(userFromDB.getPhotoUrl())
              .numLogins(userFromDB.getNumLogins() + 1)
              .nonce(authToken.nonce())
              .build());
    }

    // generating JWT
    String jwt =
        jwtService.generateJWT(
            userFromDB != null ? userFromDB.getEmail() : userFromGoogle.getEmail(),
            AuthProvider.GOOGLE,
            Role.USER);

    log.debug("[GOOGLE SERVICE] JWT generated = " + jwt);
    return jwt;
  }

  private UserEntity convertPeopleToUser(People people, long nonce) throws SocialAPIException {
    EmailAddress email =
        people.getEmailAddresses().stream()
            .filter(
                emailAddress ->
                    emailAddress.getMetadata().getPrimary() != null
                        && emailAddress.getMetadata().getPrimary())
            .findFirst()
            .orElseThrow(SocialAPIException::new);

    Photo photo =
        people.getPhotos().stream()
            .filter(
                googlePhoto ->
                    googlePhoto.getMetadata().getPrimary() != null
                        && googlePhoto.getMetadata().getPrimary())
            .findFirst()
            .orElseThrow(SocialAPIException::new);

    Name name =
        people.getNames().stream()
            .filter(
                googleName ->
                    googleName.getMetadata().getPrimary() != null
                        && googleName.getMetadata().getPrimary())
            .findFirst()
            .orElseThrow(SocialAPIException::new);

    return UserEntity.builder()
        .id(null)
        .name(name.getDisplayName())
        .photoUrl(photo.getUrl())
        .email(email.getValue())
        .validated(true)
        .role(Role.USER)
        .provider(AuthProvider.GOOGLE)
        .password("")
        .nonce(nonce)
        .build();
  }
}