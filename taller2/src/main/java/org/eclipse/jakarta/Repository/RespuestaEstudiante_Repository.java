package org.eclipse.jakarta.Repository;

import org.eclipse.jakarta.Model.RespuestaEstudiante;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class RespuestaEstudiante_Repository {

    @PersistenceContext(unitName = "estudiantesPU")
    private EntityManager entityManager;

    public void guardar(RespuestaEstudiante respuesta) {
        entityManager.persist(respuesta);
    }
}