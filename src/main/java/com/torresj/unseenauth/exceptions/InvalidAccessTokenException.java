package com.torresj.unseenauth.exceptions;

public class InvalidAccessTokenException extends Exception {
  public InvalidAccessTokenException() {
    super("Invalid access token");
  }
}
