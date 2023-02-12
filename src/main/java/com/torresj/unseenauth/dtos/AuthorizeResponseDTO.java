package com.torresj.unseenauth.dtos;

import com.torresj.unseen.entities.Role;

public record AuthorizeResponseDTO(String email, Role role) {}
