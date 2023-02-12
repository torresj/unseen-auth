package com.torresj.unseenauth.services;

import com.torresj.unseen.entities.UserEntity;
import com.torresj.unseen.repositories.mutations.UserMutationRepository;
import com.torresj.unseen.repositories.queries.UserQueryRepository;
import com.torresj.unseenauth.exceptions.UserNotFoundException;
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
    return userQueryRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
  }

  public void save(UserEntity user) {
    log.debug("[USER SERVICE] Saving user");
    userMutationRepository.save(user);
  }
}
