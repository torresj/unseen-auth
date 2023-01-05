package com.torresj.unseenauth.exceptions;

public class UserInOtherProviderException extends Exception {
    public UserInOtherProviderException(){
        super("User already exists with other provider");
    }
}
