package com.torresj.unseenauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseenauth.dtos.*;
import com.torresj.unseenauth.dtos.google.People;
import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.Role;
import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.repositories.mutations.UserMutationRepository;
import com.torresj.unseenauth.repositories.queries.UserQueryRepository;
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

  private MockRestServiceServer mockServer;

  private ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void init() {
    userMutationRepository.deleteAll();
  }

  @Test
  @DisplayName("Unseen Login integration test")
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
  @DisplayName("Login authorization test")
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
}
