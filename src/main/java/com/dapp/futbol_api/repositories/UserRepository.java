package com.dapp.futbol_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dapp.futbol_api.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

}
