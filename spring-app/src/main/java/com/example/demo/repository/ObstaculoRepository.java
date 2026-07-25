package com.example.demo.repository;

import com.example.demo.model.Obstaculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObstaculoRepository extends JpaRepository<Obstaculo, Long> {
}
