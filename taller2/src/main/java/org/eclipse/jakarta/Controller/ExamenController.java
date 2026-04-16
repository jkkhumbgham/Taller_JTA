package org.eclipse.jakarta.Controller;

import org.eclipse.jakarta.DTO.EntregarExamenDTO;
import org.eclipse.jakarta.Service.NuevoExamenService;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/examen")
@RequestScoped
public class ExamenController {

    @EJB
    private NuevoExamenService service;

    @GET
    @Path("/test")
    public Response test() {
        return Response.ok("Funciona").build();
    }

    @POST
    @Path("/entregar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response entregarExamen(EntregarExamenDTO dto) {
        try {
            service.entregarExamen(
                dto.getEstudianteId(),
                dto.getExamenId(),
                dto.getRespuestas()
            );
            return Response.ok("Examen entregado correctamente").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al entregar el examen: " + e.getMessage())
                    .build();
        }
    }
}