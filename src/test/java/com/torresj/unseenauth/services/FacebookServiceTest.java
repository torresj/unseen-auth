package com.torresj.unseenauth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseenauth.dtos.AuthSocialTokenDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.facebook.FacebookUser;
import com.torresj.unseenauth.dtos.facebook.Picture;
import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.Role;
import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.exceptions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

import static com.torresj.unseenauth.utils.TestUtils.GenerateUser;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacebookServiceTest {

  private static final String RESOURCE_PATH = "src/test/resources";

  private final String email = "test@test.com";
  private final String password = "";
  private final String url = "https://facebook.com?token=";
  private final String urlPicture = "https://facebook.com/picture?token=";
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Mock private UserService userService;
  @Mock private JwtService jwtService;
  @Mock private RestTemplate restTemplate;
  private FacebookService facebookService;

  @BeforeEach
  void setUp() {
    facebookService = new FacebookService(restTemplate, userService, jwtService, url, urlPicture);
  }

  @Test
  @DisplayName("Valid signIn with new user")
  void newUserSignIn()
      throws IOException, InvalidAccessTokenException, SocialAPIException,
          NonceAlreadyUsedException, UserInOtherProviderException {
    // Mock data
    FacebookUser facebookUser =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookUser.json"), FacebookUser.class);
    Picture picture =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookPicture.json"), Picture.class);

    // Mock call to facebook
    when(restTemplate.exchange(url + "accessToken", HttpMethod.GET, null, FacebookUser.class))
        .thenReturn(new ResponseEntity(facebookUser, HttpStatus.OK));
    when(restTemplate.exchange(urlPicture + "accessToken", HttpMethod.GET, null, Picture.class))
        .thenReturn(new ResponseEntity(picture, HttpStatus.OK));
    when(jwtService.generateJWT(email, AuthProvider.FACEBOOK, Role.USER)).thenReturn("JWT");

    LoginResponseDTO response =
        facebookService.signIn(
            new AuthSocialTokenDTO("accessToken", AuthProvider.FACEBOOK, 123456789));

    Assertions.assertEquals("JWT", response.jwt());
  }

  @Test
  @DisplayName("Valid signIn with an existing user")
  void existingUserSignIn()
      throws IOException, UserNotFoundException, InvalidAccessTokenException, SocialAPIException,
          NonceAlreadyUsedException, UserInOtherProviderException {
    // Mock data
    FacebookUser facebookUser =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookUser.json"), FacebookUser.class);
    Picture picture =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookPicture.json"), Picture.class);

    // Mock user
    UserEntity userEntityMock =
        GenerateUser(email, password, Role.USER, AuthProvider.FACEBOOK, true);

    // Mock call to facebook
    when(userService.get(email)).thenReturn(userEntityMock);
    when(restTemplate.exchange(url + "accessToken", HttpMethod.GET, null, FacebookUser.class))
        .thenReturn(new ResponseEntity(facebookUser, HttpStatus.OK));
    when(restTemplate.exchange(urlPicture + "accessToken", HttpMethod.GET, null, Picture.class))
        .thenReturn(new ResponseEntity(picture, HttpStatus.OK));
    when(jwtService.generateJWT(email, AuthProvider.FACEBOOK, Role.USER)).thenReturn("JWT");

    LoginResponseDTO response =
        facebookService.signIn(
            new AuthSocialTokenDTO("accessToken", AuthProvider.FACEBOOK, 323456789));

    Assertions.assertEquals("JWT", response.jwt());
  }

  @Test
  @DisplayName("Login with an invalid nonce")
  void invalidNonceLogin() throws UserNotFoundException, IOException {
    // Mock data
    FacebookUser facebookUser =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookUser.json"), FacebookUser.class);
    Picture picture =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookPicture.json"), Picture.class);

    // Mock user
    UserEntity userEntityMock =
        GenerateUser(email, password, Role.USER, AuthProvider.FACEBOOK, true);

    // Mock call to google
    when(userService.get(email)).thenReturn(userEntityMock);
    when(restTemplate.exchange(url + "accessToken", HttpMethod.GET, null, FacebookUser.class))
        .thenReturn(new ResponseEntity(facebookUser, HttpStatus.OK));
    when(restTemplate.exchange(urlPicture + "accessToken", HttpMethod.GET, null, Picture.class))
        .thenReturn(new ResponseEntity(picture, HttpStatus.OK));

    Assertions.assertThrows(
        NonceAlreadyUsedException.class,
        () ->
            facebookService.signIn(
                new AuthSocialTokenDTO("accessToken", AuthProvider.FACEBOOK, 123456789)),
        "Nonce exception should be thrown");
  }

  @Test
  @DisplayName("Login with an user in other provider")
  void invalidProviderUnseenLogin() throws UserNotFoundException, IOException {
    // Mock data
    FacebookUser facebookUser =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookUser.json"), FacebookUser.class);
    Picture picture =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookPicture.json"), Picture.class);

    // Mock user
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.GOOGLE, true);

    // Mock call to google
    when(userService.get(email)).thenReturn(userEntityMock);
    when(restTemplate.exchange(url + "accessToken", HttpMethod.GET, null, FacebookUser.class))
        .thenReturn(new ResponseEntity(facebookUser, HttpStatus.OK));
    when(restTemplate.exchange(urlPicture + "accessToken", HttpMethod.GET, null, Picture.class))
        .thenReturn(new ResponseEntity(picture, HttpStatus.OK));

    Assertions.assertThrows(
        UserInOtherProviderException.class,
        () ->
            facebookService.signIn(
                new AuthSocialTokenDTO("accessToken", AuthProvider.FACEBOOK, 323456789)),
        "User in other provider exception should be thrown");
  }

  @Test
  @DisplayName("Login with a facebook server error")
  void googleServerError() throws IOException {
    // Mock data
    FacebookUser facebookUser =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookUser.json"), FacebookUser.class);
    Picture picture =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookPicture.json"), Picture.class);

    // Mock call to google
    when(restTemplate.exchange(url + "accessToken", HttpMethod.GET, null, FacebookUser.class))
        .thenReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));

    Assertions.assertThrows(
        SocialAPIException.class,
        () ->
            facebookService.signIn(
                new AuthSocialTokenDTO("accessToken", AuthProvider.FACEBOOK, 123456789)),
        "Social API exception should be thrown");
  }
}
