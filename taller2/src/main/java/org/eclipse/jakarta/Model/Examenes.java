package org.eclipse.jakarta.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "examenes")
public class Examenes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String materia;

    public Examenes() {
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getMateria() {
        return materia;
    }
    public void setMateria(String materia) {
        this.materia = materia;
    }
}
