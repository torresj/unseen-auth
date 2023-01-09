package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthorizeResponseDTO;
import com.torresj.unseenauth.dtos.UnseenLoginDTO;
import com.torresj.unseenauth.entities.Role;
import com.torresj.unseenauth.exceptions.InvalidPasswordException;
import com.torresj.unseenauth.exceptions.UserInOtherProviderException;
import com.torresj.unseenauth.exceptions.UserNotFoundException;
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
            throws UserNotFoundException, InvalidPasswordException, UserInOtherProviderException {
        //Validating credential
        var user = userService.validateAndGetUser(unseenLoginDTO.email(),unseenLoginDTO.password());

        //generating JWT
        String jwt = jwtService.generateJWT(user.getEmail(), user.getProvider(), user.getRole());

        //Updating user
        user.setNumLogins(user.getNumLogins() + 1);
        user.setNonce(unseenLoginDTO.nonce());
        userService.updateUser(user);

        log.info("[LOGIN SERVICE] JWT generated = "+jwt);
        return jwt;
    }

    public AuthorizeResponseDTO authorize(String jwt){
        return jwtService.validateJWT(jwt);
    }
}
