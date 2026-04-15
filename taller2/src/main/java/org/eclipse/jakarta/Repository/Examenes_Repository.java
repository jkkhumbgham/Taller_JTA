package org.eclipse.jakarta.Repository;

import org.eclipse.jakarta.Model.Examenes;

import jakarta.persistence.*;;

@PersistenceContext(unitName = "examenesPU")
public class Examenes_Repository {
    private EntityManager entityManager;

    public void guardar(Examenes examen) {
        entityManager.persist(examen);
    }
}
