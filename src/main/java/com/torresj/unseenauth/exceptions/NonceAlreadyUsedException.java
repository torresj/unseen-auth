package com.torresj.unseenauth.exceptions;

public class NonceAlreadyUsedException extends Exception{
    public NonceAlreadyUsedException(){
        super("Nonce already used");
    }
}
