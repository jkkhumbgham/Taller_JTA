package org.eclipse.jakarta.Repository;

import org.eclipse.jakarta.Model.Estudiantes;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class Estudiantes_Repository {

    @PersistenceContext(unitName = "estudiantesPU")
    private EntityManager entityManager;

    public void guardar(Estudiantes estudiante) {
        entityManager.persist(estudiante);
    }
    public Estudiantes buscarPorId(Long id) {
        return entityManager.find(Estudiantes.class, id);
    }
}
