package com.example.demo.controller;

import com.example.demo.model.Obstaculo;
import com.example.demo.repository.ObstaculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/obstaculos")
public class ObstaculoController {

    private final ObstaculoRepository obstaculoRepository;

    public ObstaculoController(ObstaculoRepository obstaculoRepository) {
        this.obstaculoRepository = obstaculoRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Obstaculo criarObstaculo(@RequestBody Obstaculo obstaculo) {
        return obstaculoRepository.save(obstaculo);
    }

    @GetMapping
    public List<Obstaculo> listarObstaculos() {
        return obstaculoRepository.findAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarObstaculo(@PathVariable Long id) {
        obstaculoRepository.deleteById(id);
    }
}
