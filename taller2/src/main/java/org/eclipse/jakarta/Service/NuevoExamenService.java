package org.eclipse.jakarta.Service;


import org.eclipse.jakarta.Model.Estudiantes;
import org.eclipse.jakarta.Model.Examenes;
import org.eclipse.jakarta.Repository.Estudiantes_Repository;
import org.eclipse.jakarta.Repository.Examenes_Repository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;


public class NuevoExamenService {
    @Inject
    private Estudiantes_Repository estudiantesRepository;
    @Inject
    private Examenes_Repository examenesRepository;

    @Transactional
    public void crearNuevoExamen(String nombre, String materia, String nombreEstudiante, String apellidoEstudiante, String correoEstudiante) {
        Examenes nuevoExamen = new Examenes();
        nuevoExamen.setNombre(nombre);
        nuevoExamen.setMateria(materia);
        examenesRepository.guardar(nuevoExamen);
        Estudiantes nuevoEstudiante = new Estudiantes();
        nuevoEstudiante.setNombre(nombreEstudiante);
        nuevoEstudiante.setApellido(apellidoEstudiante);
        nuevoEstudiante.setCorreo(correoEstudiante);
        estudiantesRepository.guardar(nuevoEstudiante);
    }

    public static void main(String[] args) {
        NuevoExamenService servicio = new NuevoExamenService();
        servicio.crearNuevoExamen("Examen de Matemáticas", "Matemáticas", "Juan", "Pérez", "juan.perez@example.com");
    }
}
