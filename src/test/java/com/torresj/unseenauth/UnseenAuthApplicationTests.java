package com.torresj.unseenauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseenauth.dtos.AuthorizeRequestDTO;
import com.torresj.unseenauth.dtos.AuthorizeResponseDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.UnseenLoginDTO;
import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.Role;
import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.repositories.mutations.UserMutationRepository;
import com.torresj.unseenauth.repositories.queries.UserQueryRepository;
import com.torresj.unseenauth.services.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UnseenAuthApplicationTests {

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

  @Test
  @DisplayName("Unseen Login integration test")
  void unseenLogin() throws Exception {
    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            generateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

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
    Assertions.assertEquals(email, response.userName());
    Assertions.assertEquals(user.getNumLogins() + 1, userDB.getNumLogins());
    Assertions.assertEquals(unseenLoginDTO.nonce(), userDB.getNonce());
  }

  @Test
  @DisplayName("Unseen Login integration test")
  void dashboardUnseenLogin() throws Exception {
    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            generateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

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
    Assertions.assertEquals(email, response.userName());
    Assertions.assertEquals(user.getNumLogins() + 1, userDB.getNumLogins());
    Assertions.assertEquals(unseenLoginDTO.nonce(), userDB.getNonce());
  }

  @Test
  @DisplayName("Unseen Login authorization test")
  void unseenLoginAuthorization() throws Exception {
    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            generateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));

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

  private UserEntity generateUser(
      String email, String password, Role role, AuthProvider provider, boolean validated) {
    return new UserEntity(
        1l,
        LocalDateTime.now(),
        LocalDateTime.now(),
        email,
        password,
        role,
        LocalDateTime.now(),
        email,
        null,
        1,
        validated,
        provider,
        123456789);
  }
}
