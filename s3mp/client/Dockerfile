FROM openjdk:11

COPY Client.java Client.java
COPY selfsigned3.crt selfsigned3.crt
RUN keytool -importcert -file selfsigned3.crt -alias selfsigned -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit --noprompt
ENTRYPOINT ["java", "Client.java"]