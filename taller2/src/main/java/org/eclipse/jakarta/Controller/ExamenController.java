package org.eclipse.jakarta.Controller;

import org.eclipse.jakarta.Service.NuevoExamenService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/examen")
@RequestScoped
public class ExamenController {

    @Inject
    private NuevoExamenService service;

    @GET
    @Path("/test")
    public Response test() {
        return Response.ok("Funciona").build();
    }

    @POST
    @Path("/crear")
    public Response crear() {
        service.crearNuevoExamen(
            "Examen",
            "Matemáticas",
            "Juan",
            "Pérez",
            "correo@test.com"
        );
        return Response.ok("Creado").build();
    }
}