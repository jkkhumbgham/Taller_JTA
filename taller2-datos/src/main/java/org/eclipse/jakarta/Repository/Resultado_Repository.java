package org.eclipse.jakarta.Repository;

import org.eclipse.jakarta.Model.Resultado;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class Resultado_Repository {

    @PersistenceContext(unitName = "estudiantesPU")
    private EntityManager entityManager;

    public void guardar(Resultado resultado) {
        entityManager.persist(resultado);
    }
}