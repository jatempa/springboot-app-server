package com.mystore.app.repository;

import com.mystore.app.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findByManager_EmployeeId(Integer managerId);
    Optional<Employee> findByEmail(String email);
}