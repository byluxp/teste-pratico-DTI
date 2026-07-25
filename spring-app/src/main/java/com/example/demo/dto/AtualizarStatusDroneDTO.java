package com.example.demo.dto;

import com.example.demo.model.StatusDrone;

public class AtualizarStatusDroneDTO {
    private StatusDrone status;

    public AtualizarStatusDroneDTO() {}

    public StatusDrone getStatus() {
        return status;
    }

    public void setStatus(StatusDrone status) {
        this.status = status;
    }
}
