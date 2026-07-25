package com.example.demo.controller;

import com.example.demo.model.Pedido;
import com.example.demo.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido criarPedido(@RequestBody Pedido pedido) {
        return pedidoService.criarPedido(pedido);
    }

    @PatchMapping("/{id}/finalizar")
    public Pedido finalizarPedido(@PathVariable Long id) {
        return pedidoService.finalizarPedido(id);
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.listarTodos();
    }
}
