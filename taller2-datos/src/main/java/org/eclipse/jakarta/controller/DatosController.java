package org.eclipse.jakarta.controller;

import java.util.List;
import java.util.Map;

import org.eclipse.jakarta.Model.Estudiantes;
import org.eclipse.jakarta.Model.Examenes;
import org.eclipse.jakarta.Model.Opcion;
import org.eclipse.jakarta.Model.Pregunta;
import org.eclipse.jakarta.Model.RespuestaEstudiante;
import org.eclipse.jakarta.Model.Resultado;
import org.eclipse.jakarta.Repository.Estudiantes_Repository;
import org.eclipse.jakarta.Repository.Examenes_Repository;
import org.eclipse.jakarta.Repository.Opcion_Repository;
import org.eclipse.jakarta.Repository.Pregunta_Repository;
import org.eclipse.jakarta.Repository.RespuestaEstudiante_Repository;
import org.eclipse.jakarta.Repository.Resultado_Repository;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/datos")
@RequestScoped
public class DatosController {

    @EJB private Estudiantes_Repository estudiantesRepo;
    @EJB private Examenes_Repository examenesRepo;
    @EJB private Pregunta_Repository preguntaRepo;
    @EJB private Opcion_Repository opcionRepo;
    @EJB private RespuestaEstudiante_Repository respuestaRepo;
    @EJB private Resultado_Repository resultadoRepo;

    @POST
    @Path("/entregar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response entregarExamen(EntregarDTO dto) {
        Estudiantes estudiante = estudiantesRepo.buscarPorId(dto.getEstudianteId());
        Examenes examen = examenesRepo.buscarPorId(dto.getExamenId());

        if (estudiante == null || examen == null)
            return Response.status(400).entity("Estudiante o examen no encontrado").build();

        List<Pregunta> preguntas = preguntaRepo.buscarPorExamen(dto.getExamenId());
        int correctas = 0;

        for (Pregunta p : preguntas) {
            Long opcionId = dto.getRespuestas().get(p.getId());
            if (opcionId == null) continue;
            Opcion opcion = opcionRepo.buscarPorId(opcionId);
            if (opcion == null) continue;

            RespuestaEstudiante r = new RespuestaEstudiante();
            r.setEstudianteId(dto.getEstudianteId());
            r.setPregunta(p);
            r.setOpcion(opcion);
            respuestaRepo.guardar(r);

            if (opcion.isEsCorrecta()) correctas++;
        }

        double nota = preguntas.isEmpty() ? 0 : ((double) correctas / preguntas.size()) * 100;

        Resultado resultado = new Resultado();
        resultado.setEstudianteId(dto.getEstudianteId());
        resultado.setExamenId(dto.getExamenId());
        resultado.setNota(nota);
        resultadoRepo.guardar(resultado);

        JsonObject json = Json.createObjectBuilder()
            .add("nombreEstudiante", estudiante.getNombre())
            .add("correoEstudiante", estudiante.getCorreo())
            .add("materia", examen.getMateria())
            .add("nota", nota)
            .build();

        return Response.ok(json).build();
    }

    // DTO interno
    public static class EntregarDTO {
        private Long estudianteId;
        private Long examenId;
        private Map<Long, Long> respuestas;

        public Long getEstudianteId() { return estudianteId; }
        public void setEstudianteId(Long v) { this.estudianteId = v; }
        public Long getExamenId() { return examenId; }
        public void setExamenId(Long v) { this.examenId = v; }
        public Map<Long, Long> getRespuestas() { return respuestas; }
        public void setRespuestas(Map<Long, Long> v) { this.respuestas = v; }
    }
}