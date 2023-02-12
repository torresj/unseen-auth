package com.torresj.unseenauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.torresj.unseen.entities")
public class UnseenAuthApplication {

  public static void main(String[] args) {
    SpringApplication.run(UnseenAuthApplication.class, args);
  }
}
