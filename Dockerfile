FROM adoptopenjdk/openjdk11:latest

USER root

RUN apt-get update && apt-get install -y --no-install-recommends libfontconfig1 && rm -rf /var/lib/apt/lists/*

WORKDIR /home/egrid
COPY build/libs/*.jar /home/egrid/egrid-service.jar
RUN cd /home/egrid && \
    chown -R 1001:0 /home/egrid && \
    chmod -R g+rw /home/egrid && \
    ls -la /home/egrid

USER 1001
EXPOSE 8080
CMD java -jar egrid-service.jar 
