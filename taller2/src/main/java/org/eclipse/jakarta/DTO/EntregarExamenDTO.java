package org.eclipse.jakarta.DTO;

import java.util.Map;

public class EntregarExamenDTO {
    private Long estudianteId;
    private Long examenId;
    private Map<Long, Long> respuestas; // preguntaId -> opcionId

    public EntregarExamenDTO() {}

    public Long getEstudianteId() {
        return estudianteId;
    }
    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }
    public Long getExamenId() {
        return examenId;
    }
    public void setExamenId(Long examenId) {
        this.examenId = examenId;
    }
    public Map<Long, Long> getRespuestas() {
        return respuestas;
    }
    public void setRespuestas(Map<Long, Long> respuestas) {
        this.respuestas = respuestas;
    }
}