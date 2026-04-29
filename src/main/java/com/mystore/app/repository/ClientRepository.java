package com.mystore.app.repository;

import com.mystore.app.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    @Override
    @EntityGraph(attributePaths = "region")
    Page<Client> findAll(Pageable pageable);

    Optional<Client> findByEmail(String email);
}
