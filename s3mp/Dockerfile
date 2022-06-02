FROM openjdk:11

COPY target/*.war s3mp.war
COPY keystore2.p12 keystore2.p12
COPY selfsigned3.crt selfsigned3.crt
ENTRYPOINT ["java","-jar","s3mp.war"]