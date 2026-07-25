package com.example.demo.repository;

import com.example.demo.model.Voo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VooRepository extends JpaRepository<Voo, Long> {
}
