package com.torresj.unseenauth.exceptions;

public class InvalidPasswordException extends Exception {
  public InvalidPasswordException() {
    super("Invalid password");
  }
}
