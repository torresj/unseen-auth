package com.torresj.unseenauth.exceptions;

public class UserNotAnAdminException extends Exception {
  public UserNotAnAdminException() {
    super("User is not Admin");
  }
}
