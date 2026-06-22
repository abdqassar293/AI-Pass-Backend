package com.HOPn.AI_Pass.service;


import com.HOPn.AI_Pass.repository.UserRepository;
import com.HOPn.AI_Pass.model.UserEntity;
import com.HOPn.AI_Pass.model.UserRoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserRepository userEntityRepository;

    public UserDetailsService(UserRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userEntityRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        return new User(user.getEmail(), user.getPassword(), mapRoleToGrantedAuthority(user.getRole()));
    }

    private Collection<GrantedAuthority> mapRoleToGrantedAuthority(UserRoleEnum role) {
        return Collections.singleton(new SimpleGrantedAuthority(role.name()));
    }
}