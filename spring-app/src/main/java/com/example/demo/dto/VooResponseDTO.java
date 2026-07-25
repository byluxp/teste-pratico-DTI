package com.example.demo.dto;

public class VooResponseDTO {
    private Long id;
    private String mensagem;

    public VooResponseDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
