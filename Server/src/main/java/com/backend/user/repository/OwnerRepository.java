package com.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.user.entity.Owner;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

}
