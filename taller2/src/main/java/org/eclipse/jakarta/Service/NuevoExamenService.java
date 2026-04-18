package org.eclipse.jakarta.Service;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.eclipse.jakarta.Util.Correos;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Stateless
public class NuevoExamenService {

    private static final String DATOS_BASE_URL = "http://wildfly-datos:8080/taller2-datos/api";

    @EJB
    private Correos correo;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void entregarExamen(Long estudianteId, Long examenId, Map<Long, Long> respuestas) {
        try {
            // Construir JSON
            StringBuilder respJson = new StringBuilder("{");
            respuestas.forEach((p, o) ->
                respJson.append("\"").append(p).append("\":").append(o).append(","));
            if (respJson.charAt(respJson.length() - 1) == ',')
                respJson.deleteCharAt(respJson.length() - 1);
            respJson.append("}");

            String body = String.format(
                "{\"estudianteId\":%d,\"examenId\":%d,\"respuestas\":%s}",
                estudianteId, examenId, respJson);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DATOS_BASE_URL + "/datos/entregar"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new RuntimeException("Error en capa de datos: " + response.body());

            JsonReader jr = Json.createReader(new StringReader(response.body()));
            JsonObject resultado = jr.readObject();

            correo.enviarCorreo(
                "juancamiloalbac@gmail.com",
                "Resultado de tu examen",
                "Hola " + resultado.getString("nombreEstudiante") +
                ", tu nota en " + resultado.getString("materia") +
                " fue: " + resultado.getJsonNumber("nota").doubleValue() + "/100"
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al entregar el examen: " + e.getMessage(), e);
        }
    }
}