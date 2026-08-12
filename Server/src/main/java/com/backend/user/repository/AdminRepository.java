package com.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.user.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

}
