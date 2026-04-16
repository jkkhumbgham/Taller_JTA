package org.eclipse.jakarta.Service;

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
import org.eclipse.jakarta.Util.Correos;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;

@Stateless
public class NuevoExamenService {

    @EJB
    private Estudiantes_Repository estudiantesRepository;
    @EJB
    private Examenes_Repository examenesRepository;
    @EJB
    private Pregunta_Repository preguntaRepository;
    @EJB
    private Opcion_Repository opcionRepository;
    @EJB
    private RespuestaEstudiante_Repository respuestaRepository;
    @EJB
    private Resultado_Repository resultadoRepository;
    @EJB
    private Correos correo;

    @Transactional
    public void entregarExamen(Long estudianteId, Long examenId, Map<Long, Long> respuestas) {

        // 1. Buscar estudiante y examen
        Estudiantes estudiante = estudiantesRepository.buscarPorId(estudianteId);
        Examenes examen = examenesRepository.buscarPorId(examenId);

        if (estudiante == null || examen == null) {
            throw new IllegalArgumentException("Estudiante o examen no encontrado");
        }

        // 2. Obtener preguntas del examen
        List<Pregunta> preguntas = preguntaRepository.buscarPorExamen(examenId);

        int correctas = 0;

        // 3. Guardar cada respuesta y contar las correctas
        for (Pregunta pregunta : preguntas) {
            Long opcionSeleccionadaId = respuestas.get(pregunta.getId());
            if (opcionSeleccionadaId == null) continue;

            Opcion opcionSeleccionada = opcionRepository.buscarPorId(opcionSeleccionadaId);
            if (opcionSeleccionada == null) continue;

            RespuestaEstudiante respuesta = new RespuestaEstudiante();
            respuesta.setEstudiante(estudiante);
            respuesta.setPregunta(pregunta);
            respuesta.setOpcion(opcionSeleccionada);
            respuestaRepository.guardar(respuesta);

            if (opcionSeleccionada.isEsCorrecta()) {
                correctas++;
            }
        }

        // 4. Calcular nota (sobre 100)
        double nota = preguntas.isEmpty() ? 0 : ((double) correctas / preguntas.size()) * 100;

        // 5. Guardar resultado
        Resultado resultado = new Resultado();
        resultado.setEstudiante(estudiante);
        resultado.setExamen(examen);
        resultado.setNota(nota);
        resultadoRepository.guardar(resultado);

        // 6. Notificar por correo
        correo.enviarCorreo(
            estudiante.getCorreo(),
            "Resultado de tu examen",
            "Hola " + estudiante.getNombre() + ", tu nota en " + examen.getMateria() + " fue: " + nota + "/100"
        );
    }
}