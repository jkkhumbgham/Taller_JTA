package org.eclipse.jakarta.Repository;

import org.eclipse.jakarta.Model.Estudiantes;

import jakarta.persistence.*;


@PersistenceContext(unitName = "estudiantesPU")
public class Estudiantes_Repository {
    private EntityManager entityManager;

    public void guardar(Estudiantes estudiante) {
        entityManager.persist(estudiante);
    }
}
