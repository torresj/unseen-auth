package com.torresj.unseenauth.controllers;

import com.torresj.unseenauth.dtos.*;
import com.torresj.unseenauth.exceptions.*;
import com.torresj.unseenauth.services.LoginService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

  @Operation(summary = "Login with user and password")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = LoginResponseDTO.class))
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDTO> login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Unseen login DTO with user and password",
              required = true,
              content = @Content(schema = @Schema(implementation = UnseenLoginDTO.class)))
          @RequestBody
          UnseenLoginDTO unseenLoginDTO) {
    try {
      log.info("[UNSEEN LOGIN] Login for user " + unseenLoginDTO.email());

      LoginResponseDTO response = loginService.unseenLogin(unseenLoginDTO);

      log.info("[UNSEEN LOGIN] Login for user " + unseenLoginDTO.email() + " success");
      return ResponseEntity.ok(response);

    } catch (UserNotFoundException | InvalidPasswordException exception) {
      log.warn("[UNSEEN LOGIN] Invalid credentials");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    } catch (UserInOtherProviderException exception) {
      log.warn("[UNSEEN LOGIN] User already exists with other provider");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong provider");
    } catch (UserNotValidatedException exception) {
      log.warn("[UNSEEN LOGIN] User not validated");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not validated");
    } catch (NonceAlreadyUsedException exception) {
      log.warn("[UNSEEN LOGIN] Nonce already used");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nonce already used");
    } catch (JwtException exception) {
      log.error("[UNSEEN LOGIN] JWT exception : " + exception.getMessage());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }
  }

  @Operation(summary = "Login with OAuth token")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = LoginResponseDTO.class))
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @PostMapping("/social/login")
  public ResponseEntity<LoginResponseDTO> socialLogin(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "login DTO with OAuth token",
              required = true,
              content = @Content(schema = @Schema(implementation = AuthSocialTokenDTO.class)))
          @RequestBody
          AuthSocialTokenDTO authSocialTokenDTO) {
    try {
      log.info("[SOCIAL LOGIN] Social Login for " + authSocialTokenDTO.provider().name());

      LoginResponseDTO response = loginService.socialLogin(authSocialTokenDTO);

      log.info("[SOCIAL LOGIN] Login success");
      return ResponseEntity.ok(response);

    } catch (UserInOtherProviderException exception) {
      log.warn("[SOCIAL LOGIN] User already exists with other provider");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong provider");
    } catch (NonceAlreadyUsedException exception) {
      log.warn("[SOCIAL LOGIN] Nonce already used");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nonce already used");
    } catch (JwtException exception) {
      log.error("[SOCIAL LOGIN] JWT exception : " + exception.getMessage());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
    } catch (InvalidAccessTokenException exception) {
      log.error("[SOCIAL LOGIN] Invalid access token");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
    } catch (SocialAPIException exception) {
      log.error("[SOCIAL LOGIN] Error with Social provider API server");
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Error with Social provider API server");
    } catch (ProviderImplementationNotFoundException e) {
      log.error("[SOCIAL LOGIN] Provider has not implementation yet");
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Provider has not implementation yet");
    }
  }

  @Operation(summary = "Login for admin users in Unseen dashboard with user and password")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = LoginResponseDTO.class))
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @PostMapping("/dashboard/login")
  public ResponseEntity<LoginResponseDTO> dashboardLogin(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Unseen login DTO with user and password",
              required = true,
              content = @Content(schema = @Schema(implementation = UnseenLoginDTO.class)))
          @RequestBody
          UnseenLoginDTO unseenLoginDTO) {
    try {
      log.info("[UNSEEN DASHBOARD LOGIN] Login for user " + unseenLoginDTO.email());

      LoginResponseDTO response = loginService.dashboardLogin(unseenLoginDTO);

      log.info("[UNSEEN DASHBOARD LOGIN] Login for user " + unseenLoginDTO.email() + " success");
      return ResponseEntity.ok(response);

    } catch (UserNotFoundException | InvalidPasswordException exception) {
      log.warn("[UNSEEN DASHBOARD LOGIN] Invalid credentials");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    } catch (UserInOtherProviderException exception) {
      log.warn("[UNSEEN DASHBOARD LOGIN] User already exists with other provider");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong provider");
    } catch (UserNotAnAdminException exception) {
      log.error("[UNSEEN DASHBOARD LOGIN] User is not Admin");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not Admin");
    } catch (UserNotValidatedException exception) {
      log.warn("[UNSEEN DASHBOARD LOGIN] User not validated");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not validated");
    } catch (NonceAlreadyUsedException exception) {
      log.warn("[UNSEEN DASHBOARD LOGIN] Nonce already used");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nonce already used");
    } catch (JwtException exception) {
      log.error("[UNSEEN DASHBOARD LOGIN] JWT exception : " + exception.getMessage());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }
  }

  @Operation(summary = "Authorize Unseen JWT token")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authorized",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = AuthorizeResponseDTO.class))
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @PostMapping("/authorize")
  public ResponseEntity<AuthorizeResponseDTO> authorize(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Unseen Authorization DTO with JWT to be verified",
              required = true,
              content = @Content(schema = @Schema(implementation = AuthorizeRequestDTO.class)))
          @RequestBody
          AuthorizeRequestDTO authorizeRequestDTO) {
    try {
      log.info("[UNSEEN AUTHORIZE] validating jwt " + authorizeRequestDTO.jwt());

      var authorizeResponseDTO = loginService.authorize(authorizeRequestDTO.jwt());

      return ResponseEntity.ok(authorizeResponseDTO);

    } catch (JwtException exception) {
      log.error("[UNSEEN AUTHORIZE] JWT exception : " + exception.getMessage());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }
  }
}
