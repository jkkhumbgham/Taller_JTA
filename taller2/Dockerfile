FROM quay.io/wildfly/wildfly:latest
USER root
USER jboss
COPY target/*.war /opt/jboss/wildfly/standalone/deployments/
EXPOSE 8080 9990
USER root
CMD ["/bin/sh", "-c", "chown -R jboss:jboss /opt/jboss/wildfly && /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0"]