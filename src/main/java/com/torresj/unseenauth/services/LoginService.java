package com.torresj.unseenauth.services;

import com.torresj.unseen.entities.AuthProvider;
import com.torresj.unseen.entities.Role;
import com.torresj.unseen.entities.UserEntity;
import com.torresj.unseenauth.dtos.AuthSocialTokenDTO;
import com.torresj.unseenauth.dtos.AuthorizeResponseDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.UnseenLoginDTO;
import com.torresj.unseenauth.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {
  private final UserService userService;
  private final JwtService jwtService;
  private final Map<String, AuthSocialLogin> authSocialLoginMap;

  public LoginResponseDTO unseenLogin(UnseenLoginDTO unseenLoginDTO)
      throws UserNotFoundException, InvalidPasswordException, UserInOtherProviderException,
          UserNotValidatedException, NonceAlreadyUsedException {

    // Validating user
    log.debug("[LOGIN SERVICE] Validating user " + unseenLoginDTO.email());
    UserEntity user = checkUser(unseenLoginDTO, AuthProvider.UNSEEN);

    // generating JWT
    String jwt = jwtService.generateJWT(user.getEmail(), user.getProvider(), user.getRole());

    // Updating user
    updateUser(user, unseenLoginDTO.nonce());

    log.debug("[LOGIN SERVICE] JWT generated = " + jwt);
    return new LoginResponseDTO(jwt, user.getEmail());
  }

  public LoginResponseDTO dashboardLogin(UnseenLoginDTO unseenLoginDTO)
      throws UserNotFoundException, InvalidPasswordException, UserInOtherProviderException,
          UserNotValidatedException, NonceAlreadyUsedException, UserNotAnAdminException {

    // Validating user
    log.debug("[LOGIN SERVICE] Validating user " + unseenLoginDTO.email());
    UserEntity user = checkUser(unseenLoginDTO, AuthProvider.UNSEEN);

    // Check role
    if (!user.getRole().equals(Role.ADMIN)) throw new UserNotAnAdminException();

    // generating JWT
    String jwt = jwtService.generateJWT(user.getEmail(), user.getProvider(), user.getRole());

    // Updating user
    updateUser(user, unseenLoginDTO.nonce());

    log.debug("[LOGIN SERVICE] JWT generated = " + jwt);
    return new LoginResponseDTO(jwt, user.getEmail());
  }

  public LoginResponseDTO socialLogin(AuthSocialTokenDTO authToken)
      throws InvalidAccessTokenException, SocialAPIException, NonceAlreadyUsedException,
          UserInOtherProviderException, ProviderImplementationNotFoundException {
    log.debug("[LOGIN SERVICE] Social login for " + authToken.provider().name());

    // Check provider to use the correct auth implementation
    AuthSocialLogin autService = authSocialLoginMap.get(authToken.provider().name());

    // Check if provider implementation exists
    if (autService == null) throw new ProviderImplementationNotFoundException();

    // Sign in
    try {
      return autService.signIn(authToken);
    } catch (HttpClientErrorException exception) {
      log.error("[LOGIN SERVICE] Error calling provider server: " + exception.getMessage());
      throw new SocialAPIException();
    }
  }

  public AuthorizeResponseDTO authorize(String jwt) {
    log.debug("[LOGIN SERVICE] Validating JWT = " + jwt);
    return jwtService.validateJWT(jwt);
  }

  private UserEntity checkUser(UnseenLoginDTO unseenLoginDTO, AuthProvider provider)
      throws UserNotValidatedException, InvalidPasswordException, UserInOtherProviderException,
          NonceAlreadyUsedException, UserNotFoundException {
    // Getting user
    UserEntity user = userService.get(unseenLoginDTO.email());

    // Validating user
    if (!user.isValidated()) throw new UserNotValidatedException();
    if (!user.getPassword().equals(unseenLoginDTO.password())) throw new InvalidPasswordException();
    if (!user.getProvider().equals(provider)) throw new UserInOtherProviderException();
    if (user.getNonce() >= unseenLoginDTO.nonce()) throw new NonceAlreadyUsedException();

    return user;
  }

  private void updateUser(UserEntity user, long nonce) {
    // Updating user
    user.setNumLogins(user.getNumLogins() + 1);
    user.setNonce(nonce);
    userService.save(user);
  }
}
