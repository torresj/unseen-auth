package com.torresj.unseenauth.exceptions;

public class ProviderImplementationNotFoundException extends Exception {
  public ProviderImplementationNotFoundException() {
    super("This provider has not implementation yet");
  }
}
