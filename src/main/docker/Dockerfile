FROM cambierr/minijava

ENV cassandra_keyspace=lynx
ENV cassandra_contactpoints=127.0.0.1
ENV cassandra_username=
ENV cassandra_password=
    
COPY server.jar /server.jar


ENTRYPOINT ["java", "-jar", "-Xms512M", "-server", "-XX:+UseParNewGC", "-XX:+ScavengeBeforeFullGC", "-XX:+UseConcMarkSweepGC", "-XX:+CMSParallelRemarkEnabled", "/server.jar"]

EXPOSE 8080
