package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthorizeResponseDTO;
import com.torresj.unseenauth.dtos.UnseenLoginDTO;
import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {
  private final UserService userService;
  private final JwtService jwtService;

  public String UnseenLogin(UnseenLoginDTO unseenLoginDTO)
      throws UserNotFoundException, InvalidPasswordException, UserInOtherProviderException,
          UserNotValidatedException, NonceAlreadyUsedException {

    // Validating user
    log.debug("[LOGIN SERVICE] Validating user " + unseenLoginDTO.email());
    UserEntity user = checkUser(unseenLoginDTO, AuthProvider.UNSEEN);

    // generating JWT
    String jwt = jwtService.generateJWT(user.getEmail(), user.getProvider(), user.getRole());

    // Updating user
    updateUser(user,unseenLoginDTO.nonce());

    log.debug("[LOGIN SERVICE] JWT generated = " + jwt);
    return jwt;
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

  private void updateUser(UserEntity user, long nonce){
    // Updating user
    user.setNumLogins(user.getNumLogins() + 1);
    user.setNonce(nonce);
    userService.update(user);
  }
}
