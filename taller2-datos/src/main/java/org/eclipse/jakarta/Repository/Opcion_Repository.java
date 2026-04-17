package org.eclipse.jakarta.Repository;

import org.eclipse.jakarta.Model.Opcion;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class Opcion_Repository {

    @PersistenceContext(unitName = "examenesPU")
    private EntityManager entityManager;

    public Opcion buscarPorId(Long id) {
        return entityManager.find(Opcion.class, id);
    }
}