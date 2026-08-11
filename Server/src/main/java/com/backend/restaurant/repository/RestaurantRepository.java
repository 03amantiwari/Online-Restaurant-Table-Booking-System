package com.backend.restaurant.repository;

import com.backend.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // ---- Uniqueness checks (restaurant name is globally unique - see @Table
    // uniqueConstraints on Restaurant) ----

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}