package com.torresj.unseenauth.services;

import com.torresj.unseenauth.entities.AuthProvider;
import com.torresj.unseenauth.entities.UserEntity;
import com.torresj.unseenauth.exceptions.InvalidPasswordException;
import com.torresj.unseenauth.exceptions.UserInOtherProviderException;
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

    public UserEntity validateAndGetUser(String email, String password)
            throws UserNotFoundException, InvalidPasswordException, UserInOtherProviderException {
        log.debug("[USER SERVICE] validating user");
        var user = userQueryRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if(!user.getPassword().equals(password)) throw new InvalidPasswordException();
        if(!user.getProvider().equals(AuthProvider.UNSEEN)) throw new UserInOtherProviderException();
        return user;
    }

    public void updateUser(UserEntity user){
        log.debug("[USER SERVICE] Updating user nonce and numLogins");
        userMutationRepository.save(user);
    }
}
