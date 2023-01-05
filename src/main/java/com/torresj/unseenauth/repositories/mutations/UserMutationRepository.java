package com.torresj.unseenauth.repositories.mutations;

import com.torresj.unseenauth.entities.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserMutationRepository extends CrudRepository<UserEntity,Long> {
}
