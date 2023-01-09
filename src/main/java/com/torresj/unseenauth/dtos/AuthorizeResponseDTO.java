package com.torresj.unseenauth.dtos;

import com.torresj.unseenauth.entities.Role;

public record AuthorizeResponseDTO(String email, Role role) {
}
