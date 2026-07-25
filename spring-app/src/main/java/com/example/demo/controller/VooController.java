package com.example.demo.controller;

import com.example.demo.model.Voo;
import com.example.demo.repository.VooRepository;
import com.example.demo.dto.VooResponseDTO;
import com.example.demo.service.AlocacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entregas")
public class VooController {

    private final VooRepository vooRepository;
    private final AlocacaoService alocacaoService;

    public VooController(VooRepository vooRepository, AlocacaoService alocacaoService) {
        this.vooRepository = vooRepository;
        this.alocacaoService = alocacaoService;
    }

    @GetMapping("/rota")
    public List<Voo> listarVoos() {
        return vooRepository.findAll();
    }

    @PostMapping("/despachar")
    public ResponseEntity<List<Voo>> despacharVoo() {
        List<Voo> voos = alocacaoService.alocarPedidos();
        return ResponseEntity.ok(voos);
    }

}
