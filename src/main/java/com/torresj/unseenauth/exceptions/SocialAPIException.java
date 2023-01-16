package com.torresj.unseenauth.exceptions;

public class SocialAPIException extends Exception {
  public SocialAPIException() {
    super("Error calling social provider API");
  }
}
