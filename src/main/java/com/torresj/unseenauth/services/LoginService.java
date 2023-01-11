package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthorizeResponseDTO;
import com.torresj.unseenauth.dtos.UnseenLoginDTO;
import com.torresj.unseenauth.entities.AuthProvider;
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

    // Getting user
    var user = userService.get(unseenLoginDTO.email(), unseenLoginDTO.password());

    // Validating user
    if (!user.isValidated()) throw new UserNotValidatedException();
    if (!user.getPassword().equals(unseenLoginDTO.password())) throw new InvalidPasswordException();
    if (!user.getProvider().equals(AuthProvider.UNSEEN)) throw new UserInOtherProviderException();
    if (user.getNonce() >= unseenLoginDTO.nonce()) throw new NonceAlreadyUsedException();

    // generating JWT
    String jwt = jwtService.generateJWT(user.getEmail(), user.getProvider(), user.getRole());

    // Updating user
    user.setNumLogins(user.getNumLogins() + 1);
    user.setNonce(unseenLoginDTO.nonce());
    userService.update(user);

    log.debug("[LOGIN SERVICE] JWT generated = " + jwt);
    return jwt;
  }

  public AuthorizeResponseDTO authorize(String jwt) {
    log.debug("[LOGIN SERVICE] Validating JWT = " + jwt);
    return jwtService.validateJWT(jwt);
  }
}
