package com.torresj.unseenauth.controllers;

import com.torresj.unseenauth.dtos.AuthorizeRequestDTO;
import com.torresj.unseenauth.dtos.AuthorizeResponseDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.UnseenLoginDTO;
import com.torresj.unseenauth.exceptions.*;
import com.torresj.unseenauth.services.LoginService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/auth/")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody UnseenLoginDTO unseenLoginDTO){
        try{
            log.info("[UNSEEN LOGIN] Login for user " + unseenLoginDTO.email());

            String jwt = loginService.UnseenLogin(unseenLoginDTO);

            log.info("[UNSEEN LOGIN] Login for user " + unseenLoginDTO.email() + " success");
            return ResponseEntity.ok(new LoginResponseDTO(jwt,unseenLoginDTO.email()));

        } catch (UserNotFoundException | InvalidPasswordException exception){
            log.warn("[UNSEEN LOGIN] Invalid credentials");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (UserInOtherProviderException exception){
            log.warn("[UNSEEN LOGIN] User already exists with other provider");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong provider");
        } catch (UserNotValidatedException exception){
            log.warn("[UNSEEN LOGIN] User not validated");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not validated");
        } catch (NonceAlreadyUsedException exception){
            log.warn("[UNSEEN LOGIN] Nonce already used");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nonce already used");
        } catch (JwtException exception) {
            log.error("[UNSEEN LOGIN] JWT exception : " + exception.getMessage());
            throw  new ResponseStatusException(HttpStatus.UNAUTHORIZED,exception.getMessage());
        }
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizeResponseDTO> authorize(@RequestBody AuthorizeRequestDTO authorizeRequestDTO){
        try{
            log.info("[UNSEEN AUTHORIZE] validating jwt "+ authorizeRequestDTO.jwt());

            var authorizeResponseDTO = loginService.authorize(authorizeRequestDTO.jwt());

            return ResponseEntity.ok(authorizeResponseDTO);

        } catch (JwtException exception) {
            log.error("[UNSEEN AUTHORIZE] JWT exception : " + exception.getMessage());
            throw  new ResponseStatusException(HttpStatus.UNAUTHORIZED,exception.getMessage());
        }
    }
}
