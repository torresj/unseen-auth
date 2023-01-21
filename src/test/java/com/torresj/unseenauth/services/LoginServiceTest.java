package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthSocialTokenDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.UnseenLoginDTO;
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

import java.util.Map;

import static com.torresj.unseenauth.utils.TestUtils.GenerateUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  private final String email = "test@test.com";
  private final String password = "test";
  @Mock private UserService userService;
  @Mock private JwtService jwtService;
  @Mock private GoogleService googleService;
  @Mock private FacebookService facebookService;
  private LoginService loginService;

  @BeforeEach
  void setUp() {
    loginService = new LoginService(userService, jwtService, Map.of("GOOGLE", googleService, "FACEBOOK", facebookService));
  }

  @Test
  @DisplayName("Valid Unseen Login")
  void validUnseenLogin()
      throws UserNotFoundException, UserNotValidatedException, InvalidPasswordException,
          NonceAlreadyUsedException, UserInOtherProviderException {
    UserEntity userEntityMock =
        GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true);

    // Mocks
    when(userService.get(email)).thenReturn(userEntityMock);
    when(jwtService.generateJWT(email, AuthProvider.UNSEEN, Role.ADMIN)).thenReturn("JWT");

    LoginResponseDTO response =
        loginService.unseenLogin(new UnseenLoginDTO(email, password, 223456789));

    Assertions.assertEquals("JWT", response.jwt());
  }

  @Test
  @DisplayName("Valid Dashboard Login")
  void validDashboardUnseenLogin()
      throws UserNotFoundException, UserNotValidatedException, InvalidPasswordException,
          NonceAlreadyUsedException, UserInOtherProviderException, UserNotAnAdminException {
    UserEntity userEntityMock =
        GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true);

    // Mocks
    when(userService.get(email)).thenReturn(userEntityMock);
    when(jwtService.generateJWT(email, AuthProvider.UNSEEN, Role.ADMIN)).thenReturn("JWT");

    LoginResponseDTO response =
        loginService.dashboardLogin(new UnseenLoginDTO(email, password, 223456789));

    Assertions.assertEquals("JWT", response.jwt());
  }

  @Test
  @DisplayName("Valid Social Login with google")
  void validSocialGoogleLogin()
      throws NonceAlreadyUsedException, UserInOtherProviderException, InvalidAccessTokenException,
          SocialAPIException, ProviderImplementationNotFoundException {

    // Mocks
    when(googleService.signIn(any())).thenReturn(new LoginResponseDTO("JWT", email));

    LoginResponseDTO response =
        loginService.socialLogin(
            new AuthSocialTokenDTO("accessToken", AuthProvider.GOOGLE, 12345678));

    Assertions.assertEquals("JWT", response.jwt());
  }

  @Test
  @DisplayName("Valid Social Login with facebook")
  void validSocialFacebookLogin()
          throws NonceAlreadyUsedException, UserInOtherProviderException, InvalidAccessTokenException,
          SocialAPIException, ProviderImplementationNotFoundException {

    // Mocks
    when(facebookService.signIn(any())).thenReturn(new LoginResponseDTO("JWT", email));

    LoginResponseDTO response =
            loginService.socialLogin(
                    new AuthSocialTokenDTO("accessToken", AuthProvider.FACEBOOK, 12345678));

    Assertions.assertEquals("JWT", response.jwt());
  }

  @Test
  @DisplayName("Dashboard Login with no admin user")
  void noAdminUserDashboardUnseenLogin() throws UserNotFoundException {
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);

    // Mocks
    when(userService.get(email)).thenReturn(userEntityMock);

    Assertions.assertThrows(
        UserNotAnAdminException.class,
        () -> loginService.dashboardLogin(new UnseenLoginDTO(email, password, 223456789)),
        "User not an Admin exception should be thrown");
  }

  @Test
  @DisplayName("Unseen Login with an invalid nonce")
  void invalidNonceUnseenLogin() throws UserNotFoundException {
    UserEntity userEntityMock =
        GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true);

    // Mocks
    when(userService.get(email)).thenReturn(userEntityMock);

    Assertions.assertThrows(
        NonceAlreadyUsedException.class,
        () -> loginService.unseenLogin(new UnseenLoginDTO(email, password, 1)),
        "Nonce exception should be thrown");
  }

  @Test
  @DisplayName("Unseen Login with an invalid user")
  void invalidUserUnseenLogin() throws UserNotFoundException {
    // Mocks
    when(userService.get(email)).thenThrow(new UserNotFoundException());

    Assertions.assertThrows(
        UserNotFoundException.class,
        () -> loginService.unseenLogin(new UnseenLoginDTO(email, password, 1)),
        "User not found exception should be thrown");
  }

  @Test
  @DisplayName("Unseen Login with an invalid password")
  void invalidPasswordUnseenLogin() throws UserNotFoundException {
    UserEntity userEntityMock = GenerateUser(email, "", Role.ADMIN, AuthProvider.UNSEEN, true);

    // Mocks
    when(userService.get(email)).thenReturn(userEntityMock);

    Assertions.assertThrows(
        InvalidPasswordException.class,
        () -> loginService.unseenLogin(new UnseenLoginDTO(email, password, 223456789)),
        "Invalid password exception should be thrown");
  }

  @Test
  @DisplayName("Unseen Login with an invalid provider")
  void invalidProviderUnseenLogin() throws UserNotFoundException {
    UserEntity userEntityMock =
        GenerateUser(email, password, Role.ADMIN, AuthProvider.GOOGLE, true);

    // Mocks
    when(userService.get(email)).thenReturn(userEntityMock);

    Assertions.assertThrows(
        UserInOtherProviderException.class,
        () -> loginService.unseenLogin(new UnseenLoginDTO(email, password, 223456789)),
        "User in other provider exception should be thrown");
  }

  @Test
  @DisplayName("Unseen Login with an user not validated yet")
  void userNotValidatedUnseenLogin() throws UserNotFoundException {
    UserEntity userEntityMock =
        GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, false);

    // Mocks
    when(userService.get(email)).thenReturn(userEntityMock);

    Assertions.assertThrows(
        UserNotValidatedException.class,
        () -> loginService.unseenLogin(new UnseenLoginDTO(email, password, 223456789)),
        "User not validated exception should be thrown");
  }
}
