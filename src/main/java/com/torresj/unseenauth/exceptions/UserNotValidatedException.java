package com.torresj.unseenauth.exceptions;

public class UserNotValidatedException extends Exception {
  public UserNotValidatedException() {
    super("User not validated");
  }
}
