package com.example.demo.repository;

import com.example.demo.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, String> {
    List<Entrega> findAllByOrderByDataHoraDesc();
}
