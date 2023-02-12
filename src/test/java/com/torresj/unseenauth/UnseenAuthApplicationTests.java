package com.torresj.unseenauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseen.entities.AuthProvider;
import com.torresj.unseen.entities.Role;
import com.torresj.unseen.entities.UserEntity;
import com.torresj.unseen.repositories.mutations.UserMutationRepository;
import com.torresj.unseen.repositories.queries.UserQueryRepository;
import com.torresj.unseenauth.dtos.*;
import com.torresj.unseenauth.dtos.facebook.FacebookUser;
import com.torresj.unseenauth.dtos.facebook.Picture;
import com.torresj.unseenauth.dtos.google.People;
import com.torresj.unseenauth.services.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;

import static com.torresj.unseenauth.utils.TestUtils.GenerateUser;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UnseenAuthApplicationTests {

  private static final String RESOURCE_PATH = "src/test/resources";
  private final String email = "test@test.com";
  private final String password = "test";

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private MockMvc mockMvc;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired private UserMutationRepository userMutationRepository;
  @Autowired private UserQueryRepository userQueryRepository;
  @Autowired private JwtService jwtService;
  @Autowired private RestTemplate restTemplate;

  @Value("${social.login.google.url}")
  private String googleUrl;

  @Value("${social.login.facebook.url}")
  private String facebookUrl;

  @Value("${social.login.facebook.picture.url}")
  private String facebookPictureUrl;

  private MockRestServiceServer mockServer;

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void init() {
    userMutationRepository.deleteAll();
  }

  @Test
  @DisplayName("Unseen valid login")
  void unseenLogin() throws Exception {
    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isOk());
    // Parsing response
    var content = result.andReturn().getResponse().getContentAsString();
    LoginResponseDTO response = objectMapper.readValue(content, LoginResponseDTO.class);

    // Getting user from DB
    UserEntity userDB = userQueryRepository.findByEmail(email).get();

    // Checks
    var authResponse = jwtService.validateJWT(response.jwt());
    Assertions.assertEquals(email, authResponse.email());
    Assertions.assertEquals(Role.ADMIN, authResponse.role());
    Assertions.assertEquals(email, response.email());
    Assertions.assertEquals(user.getNumLogins() + 1, userDB.getNumLogins());
    Assertions.assertEquals(unseenLoginDTO.nonce(), userDB.getNonce());
  }

  @Test
  @DisplayName("Unseen Login with user not found")
  void unseenLoginWithUserNotFound() throws Exception {
    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Invalid credentials", error);
  }

  @Test
  @DisplayName("Unseen Login with bad credentials")
  void unseenLoginWithInvalidPassword() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, "wrongPassword", 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Invalid credentials", error);
  }

  @Test
  @DisplayName("Unseen Login with wrong provider")
  void unseenLoginWithWrongProvider() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.GOOGLE, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Wrong provider", error);
  }

  @Test
  @DisplayName("Unseen Login with user not validated")
  void unseenLoginWithUserNotValidated() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, false));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("User not validated", error);
  }

  @Test
  @DisplayName("Unseen Login with nonce already used")
  void unseenLoginWithNonceAlreadyUsed() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 1);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Nonce already used", error);
  }

  @Test
  @DisplayName("Dashboard Login integration test")
  void dashboardUnseenLogin() throws Exception {
    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/dashboard/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isOk());
    // Parsing response
    var content = result.andReturn().getResponse().getContentAsString();
    LoginResponseDTO response = objectMapper.readValue(content, LoginResponseDTO.class);

    // Getting user from DB
    UserEntity userDB = userQueryRepository.findByEmail(email).get();

    // Checks
    var authResponse = jwtService.validateJWT(response.jwt());
    Assertions.assertEquals(email, authResponse.email());
    Assertions.assertEquals(Role.ADMIN, authResponse.role());
    Assertions.assertEquals(email, response.email());
    Assertions.assertEquals(user.getNumLogins() + 1, userDB.getNumLogins());
    Assertions.assertEquals(unseenLoginDTO.nonce(), userDB.getNonce());
  }

  @Test
  @DisplayName("Dashboard Unseen Login with user not found")
  void dashboardUnseenLoginWithUserNotFound() throws Exception {
    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/dashboard/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Invalid credentials", error);
  }

  @Test
  @DisplayName("Dashboard Unseen Login with bad credentials")
  void dashboardUnseenLoginWithInvalidPassword() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, "wrongPassword", 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/dashboard/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Invalid credentials", error);
  }

  @Test
  @DisplayName("Dashboard Unseen Login with wrong provider")
  void dashboardUnseenLoginWithWrongProvider() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.GOOGLE, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/dashboard/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Wrong provider", error);
  }

  @Test
  @DisplayName("Dashboard Unseen Login with user not validated")
  void dashboardUnseenLoginWithUserNotValidated() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, false));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/dashboard/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("User not validated", error);
  }

  @Test
  @DisplayName("Dashboard Unseen Login with nonce already used")
  void dashboardUnseenLoginWithNonceAlreadyUsed() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 1);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/dashboard/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Nonce already used", error);
  }

  @Test
  @DisplayName("Dashboard Unseen Login with not an admin user")
  void dashboardUnseenLoginWithNotAnAdmin() throws Exception {

    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true));

    // Create request object
    UnseenLoginDTO unseenLoginDTO = new UnseenLoginDTO(email, password, 223456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/dashboard/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unseenLoginDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("User is not Admin", error);
  }

  @Test
  @DisplayName("Google Login with an existing user integration test")
  void googleLogin() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.GOOGLE, true));

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO =
        new AuthSocialTokenDTO("JWT", AuthProvider.GOOGLE, 323456789);

    // Create a google response
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock google server
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(googleUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(people)));

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isOk());
    // Parsing response
    var content = result.andReturn().getResponse().getContentAsString();
    LoginResponseDTO response = objectMapper.readValue(content, LoginResponseDTO.class);

    // Getting user from DB
    UserEntity userDB = userQueryRepository.findByEmail(email).get();

    // Checks
    var authResponse = jwtService.validateJWT(response.jwt());
    Assertions.assertEquals(email, authResponse.email());
    Assertions.assertEquals(Role.USER, authResponse.role());
    Assertions.assertEquals(email, response.email());
    Assertions.assertEquals(user.getNumLogins() + 1, userDB.getNumLogins());
    Assertions.assertEquals(authSocialTokenDTO.nonce(), userDB.getNonce());
  }

  @Test
  @DisplayName("Google Login with a new user integration test")
  void newUserGoogleLogin() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO =
        new AuthSocialTokenDTO("JWT", AuthProvider.GOOGLE, 323456789);

    // Create a google response
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock google server
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(googleUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(people)));

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isOk());
    // Parsing response
    var content = result.andReturn().getResponse().getContentAsString();
    LoginResponseDTO response = objectMapper.readValue(content, LoginResponseDTO.class);

    // Getting user from DB
    UserEntity userDB = userQueryRepository.findByEmail(email).get();

    // Checks
    var authResponse = jwtService.validateJWT(response.jwt());
    Assertions.assertNotNull(userDB);
    Assertions.assertEquals(email, authResponse.email());
    Assertions.assertEquals(Role.USER, authResponse.role());
    Assertions.assertEquals(email, response.email());
    Assertions.assertEquals(authSocialTokenDTO.nonce(), userDB.getNonce());
  }

  @Test
  @DisplayName("Facebook Login with an existing user integration test")
  void facebookLogin() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.FACEBOOK, true));

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO =
        new AuthSocialTokenDTO("JWT", AuthProvider.FACEBOOK, 323456789);

    // Create a facebook responses
    FacebookUser facebookUser =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookUser.json"), FacebookUser.class);
    Picture picture =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookPicture.json"), Picture.class);

    // Mock facebook server
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(facebookUrl + authSocialTokenDTO.token())))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(facebookUser)));

    mockServer
        .expect(
            ExpectedCount.once(),
            requestTo(new URI(facebookPictureUrl + authSocialTokenDTO.token())))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(picture)));

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isOk());
    // Parsing response
    var content = result.andReturn().getResponse().getContentAsString();
    LoginResponseDTO response = objectMapper.readValue(content, LoginResponseDTO.class);

    // Getting user from DB
    UserEntity userDB = userQueryRepository.findByEmail(email).get();

    // Checks
    var authResponse = jwtService.validateJWT(response.jwt());
    Assertions.assertEquals(email, authResponse.email());
    Assertions.assertEquals(Role.USER, authResponse.role());
    Assertions.assertEquals(email, response.email());
    Assertions.assertEquals(user.getNumLogins() + 1, userDB.getNumLogins());
    Assertions.assertEquals(authSocialTokenDTO.nonce(), userDB.getNonce());
  }

  @Test
  @DisplayName("Facebook Login with a new user integration test")
  void newUserFacebookLogin() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO =
        new AuthSocialTokenDTO("JWT", AuthProvider.FACEBOOK, 323456789);

    // Create a facebook responses
    FacebookUser facebookUser =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookUser.json"), FacebookUser.class);
    Picture picture =
        objectMapper.readValue(new File(RESOURCE_PATH + "/facebookPicture.json"), Picture.class);

    // Mock facebook server
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(facebookUrl + authSocialTokenDTO.token())))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(facebookUser)));

    mockServer
        .expect(
            ExpectedCount.once(),
            requestTo(new URI(facebookPictureUrl + authSocialTokenDTO.token())))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(picture)));

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isOk());
    // Parsing response
    var content = result.andReturn().getResponse().getContentAsString();
    LoginResponseDTO response = objectMapper.readValue(content, LoginResponseDTO.class);

    // Getting user from DB
    UserEntity userDB = userQueryRepository.findByEmail(email).get();

    // Checks
    var authResponse = jwtService.validateJWT(response.jwt());
    Assertions.assertEquals(email, authResponse.email());
    Assertions.assertEquals(Role.USER, authResponse.role());
    Assertions.assertEquals(email, response.email());
    Assertions.assertEquals(authSocialTokenDTO.nonce(), userDB.getNonce());
  }

  @Test
  @DisplayName("Social Login with user in other provider")
  void socialLoginWithUserInOtherProvider() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.FACEBOOK, true));

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO =
        new AuthSocialTokenDTO("JWT", AuthProvider.GOOGLE, 323456789);

    // Create a google response
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock google server
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(googleUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(people)));

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Wrong provider", error);
  }

  @Test
  @DisplayName("Social Login with nonce already used")
  void socialLoginWithNonceAlreadyUsed() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.GOOGLE, true));

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO = new AuthSocialTokenDTO("JWT", AuthProvider.GOOGLE, 1);

    // Create a google response
    People people = objectMapper.readValue(new File(RESOURCE_PATH + "/people.json"), People.class);

    // Mock google server
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(googleUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(people)));

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Nonce already used", error);
  }

  @Test
  @DisplayName("Social Login with invalid access token")
  void socialLoginWithInvalidAccessToken() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.GOOGLE, true));

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO =
        new AuthSocialTokenDTO("", AuthProvider.GOOGLE, 323456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Invalid access token", error);
  }

  @Test
  @DisplayName("Social Login with provider server error")
  void socialLoginWithProviderServerError() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.GOOGLE, true));

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO =
        new AuthSocialTokenDTO("JWT", AuthProvider.GOOGLE, 323456789);

    // Mock google server
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(googleUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Error with Social provider API server", error);
  }

  @Test
  @DisplayName("Social Login with provider not implemented")
  void socialLoginWithProviderNotImplemented() throws Exception {
    // Mock restTemplate
    mockServer = MockRestServiceServer.createServer(restTemplate);

    // Create request object
    AuthSocialTokenDTO authSocialTokenDTO =
        new AuthSocialTokenDTO("JWT", AuthProvider.TWITTER, 323456789);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/social/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authSocialTokenDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("Provider has not implementation yet", error);
  }

  @Test
  @DisplayName("Authorize valid request")
  void loginAuthorization() throws Exception {
    // Create a valid user in DB
    userMutationRepository.save(
        GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Create JWT
    String jwt = jwtService.generateJWT(email, AuthProvider.UNSEEN, Role.ADMIN);

    // Create request object
    AuthorizeRequestDTO requestDTO = new AuthorizeRequestDTO(jwt);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/authorize")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk());
    // Parsing response
    var content = result.andReturn().getResponse().getContentAsString();
    AuthorizeResponseDTO response = objectMapper.readValue(content, AuthorizeResponseDTO.class);

    // Checks
    Assertions.assertEquals(email, response.email());
    Assertions.assertEquals(Role.ADMIN, response.role());
  }

  @Test
  @DisplayName("Invalid Authorize request")
  void invalidLoginAuthorization() throws Exception {
    // Create a valid user in DB
    userMutationRepository.save(
        GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Create JWT
    String jwt = jwtService.generateJWT(email, AuthProvider.UNSEEN, Role.ADMIN) + "invalid";

    // Create request object
    AuthorizeRequestDTO requestDTO = new AuthorizeRequestDTO(jwt);

    // Post /login
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/auth/authorize")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isUnauthorized());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals(
        "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.",
        error);
  }
}
