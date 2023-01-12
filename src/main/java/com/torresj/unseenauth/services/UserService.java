package com.torresj.unseenauth.services;

import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.exceptions.UserNotFoundException;
import com.torresj.unseenauth.repositories.mutations.UserMutationRepository;
import com.torresj.unseenauth.repositories.queries.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
  private final UserQueryRepository userQueryRepository;
  private final UserMutationRepository userMutationRepository;

  public UserEntity get(String email) throws UserNotFoundException {
    log.debug("[USER SERVICE] Getting user by email " + email);
    return  userQueryRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
  }

  public void update(UserEntity user) {
    log.debug("[USER SERVICE] Updating user");
    userMutationRepository.save(user);
  }
}
