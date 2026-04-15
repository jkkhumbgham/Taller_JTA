FROM quay.io/wildfly/wildfly:latest

# =========================
# 1. Cambiar a root
# =========================
USER root

# =========================
# 2. Crear módulo PostgreSQL
# =========================
RUN mkdir -p /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/

# Descargar driver
ADD https://jdbc.postgresql.org/download/postgresql-42.7.3.jar \
    /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/postgresql.jar

# Crear module.xml
RUN echo '<?xml version="1.0" encoding="UTF-8"?> \
<module xmlns="urn:jboss:module:1.1" name="org.postgresql"> \
    <resources> \
        <resource-root path="postgresql.jar"/> \
    </resources> \
    <dependencies> \
        <module name="javax.api"/> \
        <module name="javax.transaction.api"/> \
    </dependencies> \
</module>' > /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/module.xml

# =========================
# 3. Copiar script CLI
# =========================
COPY configure.cli /opt/jboss/configure.cli

# Ejecutar configuración (modifica standalone.xml)
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/configure.cli

# =========================
# 4. Volver a usuario jboss
# =========================
USER jboss

# =========================
# 5. Copiar aplicación
# =========================
COPY target/*.war /opt/jboss/wildfly/standalone/deployments/

# =========================
# 6. Exponer puertos
# =========================
EXPOSE 8080 9990

# =========================
# 7. Ejecutar WildFly
# =========================
USER root

CMD ["/bin/sh", "-c", "chown -R jboss:jboss /opt/jboss/wildfly && /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0"]