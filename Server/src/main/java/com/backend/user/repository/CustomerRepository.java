package com.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.user.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
