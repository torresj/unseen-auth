package com.torresj.unseenauth.services;

import com.torresj.unseenauth.dtos.AuthSocialTokenDTO;
import com.torresj.unseenauth.dtos.LoginResponseDTO;
import com.torresj.unseenauth.exceptions.InvalidAccessTokenException;
import com.torresj.unseenauth.exceptions.NonceAlreadyUsedException;
import com.torresj.unseenauth.exceptions.SocialAPIException;
import com.torresj.unseenauth.exceptions.UserInOtherProviderException;

public interface AuthSocialLogin {
  LoginResponseDTO signIn(AuthSocialTokenDTO authToken)
      throws InvalidAccessTokenException, SocialAPIException, UserInOtherProviderException,
          NonceAlreadyUsedException;
}
