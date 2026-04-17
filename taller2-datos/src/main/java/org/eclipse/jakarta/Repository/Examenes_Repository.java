package org.eclipse.jakarta.Repository;

import org.eclipse.jakarta.Model.Examenes;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class Examenes_Repository {

    @PersistenceContext(unitName = "examenesPU")
    private EntityManager entityManager;

    public void guardar(Examenes examen) {
        entityManager.persist(examen);
    }

    public Examenes buscarPorId(Long id) {
        return entityManager.find(Examenes.class, id);
    }
}