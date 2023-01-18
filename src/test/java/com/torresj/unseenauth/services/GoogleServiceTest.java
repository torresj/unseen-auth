package com.torresj.unseenauth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseenauth.dtos.AuthSocialTokenDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.google.People;
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
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

import static com.torresj.unseenauth.utils.TestUtils.GenerateUser;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleServiceTest {

  private static final String RESOURCE_PATH = "src/test/resources";

  private final String email = "test@test.com";
  private final String password = "";
  private final String url = "https://google.com";
  @Mock private UserService userService;
  @Mock private JwtService jwtService;
  @Mock private RestTemplate restTemplate;
  private GoogleService googleService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    googleService = new GoogleService(restTemplate, userService, jwtService, url);
  }

  @Test
  @DisplayName("Valid signIn with new user")
  void newUserSignIn()
      throws IOException, InvalidAccessTokenException, SocialAPIException,
          NonceAlreadyUsedException, UserInOtherProviderException {
    // Mock people
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock call to google
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer accessToken");
    when(restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), People.class))
        .thenReturn(new ResponseEntity(people, HttpStatus.OK));
    when(jwtService.generateJWT(email, AuthProvider.GOOGLE, Role.USER)).thenReturn("JWT");

    LoginResponseDTO response =
        googleService.signIn(new AuthSocialTokenDTO("accessToken", AuthProvider.GOOGLE, 123456789));

    Assertions.assertEquals("JWT", response.jwt());
  }

  @Test
  @DisplayName("Valid signIn with an existing user")
  void existingUserSignIn()
      throws IOException, InvalidAccessTokenException, SocialAPIException,
          NonceAlreadyUsedException, UserInOtherProviderException, UserNotFoundException {
    // Mock people
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock user
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.GOOGLE, true);

    // Mock call to google
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer accessToken");
    when(userService.get(email)).thenReturn(userEntityMock);
    when(restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), People.class))
        .thenReturn(new ResponseEntity(people, HttpStatus.OK));
    when(jwtService.generateJWT(email, AuthProvider.GOOGLE, Role.USER)).thenReturn("JWT");

    LoginResponseDTO response =
        googleService.signIn(new AuthSocialTokenDTO("accessToken", AuthProvider.GOOGLE, 323456789));

    Assertions.assertEquals("JWT", response.jwt());
  }

  @Test
  @DisplayName("Login with an invalid nonce")
  void invalidNonceLogin() throws UserNotFoundException, IOException {
    // Mock people
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock user
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.GOOGLE, true);

    // Mock call to google
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer accessToken");
    when(userService.get(email)).thenReturn(userEntityMock);
    when(restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), People.class))
        .thenReturn(new ResponseEntity(people, HttpStatus.OK));

    Assertions.assertThrows(
        NonceAlreadyUsedException.class,
        () ->
            googleService.signIn(
                new AuthSocialTokenDTO("accessToken", AuthProvider.GOOGLE, 123456789)),
        "Nonce exception should be thrown");
  }

  @Test
  @DisplayName("Login with an user in other provider")
  void invalidProviderUnseenLogin() throws UserNotFoundException, IOException {
    // Mock people
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock user
    UserEntity userEntityMock =
        GenerateUser(email, password, Role.USER, AuthProvider.FACEBOOK, true);

    // Mock call to google
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer accessToken");
    when(userService.get(email)).thenReturn(userEntityMock);
    when(restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), People.class))
        .thenReturn(new ResponseEntity(people, HttpStatus.OK));

    Assertions.assertThrows(
        UserInOtherProviderException.class,
        () ->
            googleService.signIn(
                new AuthSocialTokenDTO("accessToken", AuthProvider.GOOGLE, 323456789)),
        "User in other provider exception should be thrown");
  }

  @Test
  @DisplayName("Login with a google server error")
  void googleServerError() throws UserNotFoundException, IOException {
    // Mock people
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock call to google
    var headers = new HttpHeaders();
    headers.set("Authorization", "Bearer accessToken");
    when(restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), People.class))
        .thenReturn(new ResponseEntity(people, HttpStatus.BAD_REQUEST));

    Assertions.assertThrows(
        SocialAPIException.class,
        () ->
            googleService.signIn(
                new AuthSocialTokenDTO("accessToken", AuthProvider.GOOGLE, 123456789)),
        "Social API exception should be thrown");
  }
}
