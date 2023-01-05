package com.torresj.unseenauth.controllers;

import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.dtos.UnseenLoginDTO;
import com.torresj.unseenauth.exceptions.InvalidPasswordException;
import com.torresj.unseenauth.exceptions.UserInOtherProviderException;
import com.torresj.unseenauth.exceptions.UserNotFoundException;
import com.torresj.unseenauth.services.LoginService;
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
            log.info("[UNSEEN LOGIN] Invalid credentials");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (UserInOtherProviderException exception){
            log.info("[UNSEEN LOGIN] User already exists with other provider");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong provider");
        }
    }
}
