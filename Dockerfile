FROM kavindut2/maven-dependency-cache AS build
WORKDIR /app
COPY pom.xml .
COPY io.asgardeo.tomcat.oidc.agent ./io.asgardeo.tomcat.oidc.agent
COPY migration-docs-app ./migration-docs-app

RUN mvn clean install -DskipTests

# Stage 3: Tomcat Setup
FROM tomcat:9.0.91-jdk17

RUN apt-get update && apt-get install -y unzip && \
    groupadd --gid 10015 choreo || true && \
    useradd --uid 10015 --gid choreo --no-create-home --shell /bin/sh choreouser || true

# Create necessary directories and set permissions as root
RUN mkdir -p /usr/local/tomcat/work/Catalina/localhost/ROOT && \
    chown -R 10015:choreo /usr/local/tomcat/webapps \
       /usr/local/tomcat/logs \
       /usr/local/tomcat/temp \
       /usr/local/tomcat/work \
       /usr/local/tomcat/conf

USER 10015

ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH

COPY server.xml $CATALINA_HOME/conf/

# Deploy WAR file 
COPY  --from=build /app/migration-docs-app/target/ROOT.war $CATALINA_HOME/webapps/ROOT.war
# COPY tomcat_webapp/io.asgardeo.tomcat.oidc.sample/target/ROOT.war $CATALINA_HOME/webapps/ROOT.war

# Unzip ROOT.war 
RUN unzip $CATALINA_HOME/webapps/ROOT.war -d $CATALINA_HOME/webapps/ROOT && rm $CATALINA_HOME/webapps/ROOT.war

RUN ls -al $CATALINA_HOME/webapps/ROOT/.

COPY oidc-sample-app.properties $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/
COPY logging.properties $CATALINA_HOME/conf/logging.properties

EXPOSE 8080

CMD ["catalina.sh", "run"]

