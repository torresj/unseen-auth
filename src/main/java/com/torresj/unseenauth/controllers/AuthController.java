package com.torresj.unseenauth.controllers;

import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/")
@Log
public class AuthController {

    @GetMapping("test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("test");
    }
}
