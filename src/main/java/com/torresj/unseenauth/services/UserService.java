package com.torresj.unseenauth.services;

import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.exceptions.InvalidPasswordException;
import com.torresj.unseenauth.exceptions.UserInOtherProviderException;
import com.torresj.unseenauth.exceptions.UserNotFoundException;
import com.torresj.unseenauth.exceptions.UserNotValidatedException;
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

    public UserEntity validateAndGetUser(String email, String password) throws UserNotFoundException {
        log.debug("[USER SERVICE] Getting user by email " + email);
        var user = userQueryRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return user;
    }

    public void updateUser(UserEntity user){
        log.debug("[USER SERVICE] Updating user");
        userMutationRepository.save(user);
    }
}
