package org.eclipse.jakarta.Repository;

import java.util.List;

import org.eclipse.jakarta.Model.Pregunta;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class Pregunta_Repository {

    @PersistenceContext(unitName = "examenesPU")
    private EntityManager entityManager;

    public List<Pregunta> buscarPorExamen(Long examenId) {
        return entityManager
            .createQuery("SELECT p FROM Pregunta p WHERE p.examen.id = :examenId", Pregunta.class)
            .setParameter("examenId", examenId)
            .getResultList();
    }

    public Pregunta buscarPorId(Long id) {
        return entityManager.find(Pregunta.class, id);
    }
}