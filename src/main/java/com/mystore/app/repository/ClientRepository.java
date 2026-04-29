package com.mystore.app.repository;

import com.mystore.app.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    @Query("select c from Client c left join fetch c.region")
    List<Client> findAllWithRegion();

    Optional<Client> findByEmail(String email);
}
