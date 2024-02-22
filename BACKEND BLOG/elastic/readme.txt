--Configuración y Instalación de Docker Elastic en Ubuntu 22.04 

-INSTALAR ELASTIC
-1 Comprobar la compatibilidad con el SO. En mi caso Ubuntu 22.04, puedo instalar 8.3.x en adelante. 
-2 Instalar docker-desktop
-3 Crear red elastic: docker network create elastic
-4 Descargar imagen elasticsearch docker: docker pull docker.elastic.co/elasticsearch/elasticsearch:8.12.0 (version a la fecha)
-5 Start contenedor docker: docker run -d --name es01 --net elastic -p 9200:9200 -it -m 1GB docker.elastic.co/elasticsearch/elasticsearch:8.12.0

  PROBLEMA con memoria: sysctl -w vm.max_map_count=262144

-6 Buscar password y certificados que genera el volumen en el log, si queremos podemos generarlos nuevamente. 
-7 Copiar certificados en diractorio local de la pc.  docker cp es01:/usr/share/elasticsearch/config/certs/http_ca.crt .


-INSTALAR CLIENTE ELASTIC EN JAVA (Installation in a Maven project by using Jackson)
-1 Agregar dependencias al archivo pom.xml
-2 actualizar repositorio para que baje los jar

-INSTALAR KIBANA
docker pull docker.elastic.co/kibana/kibana:8.12.0
docker run -d --name kib01 --net elastic -p 5601:5601 docker.elastic.co/kibana/kibana:8.12.0


docker exec -it es01 /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic
docker exec -it es01 /usr/share/elasticsearch/bin/elasticsearch-create-enrollment-token -s kibana

-CONECTAR


Bibliografia:
	https://www.elastic.co/guide/en/elasticsearch/reference/8.10/docker.html 
	https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/installation.html#maven


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ Elasticsearch security features have been automatically configured!
✅ Authentication is enabled and cluster connections are encrypted.

ℹ️  Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
  IkgEPti9wghrBHJbKSgF

ℹ️  HTTP CA certificate SHA-256 fingerprint:
  4ef7db5f8c145aadead4b07e46517760f998274a2f34ffc229bfca9c95539e83

ℹ️  Configure Kibana to use this cluster:
• Run Kibana and click the configuration link in the terminal when Kibana starts.
• Copy the following enrollment token and paste it into Kibana in your browser (valid for the next 30 minutes):
  eyJ2ZXIiOiI4LjEyLjAiLCJhZHIiOlsiMTcyLjE4LjAuMjo5MjAwIl0sImZnciI6IjRlZjdkYjVmOGMxNDVhYWRlYWQ0YjA3ZTQ2NTE3NzYwZjk5ODI3NGEyZjM0ZmZjMjI5YmZjYTljOTU1MzllODMiLCJrZXkiOiI3Zm9WZkkwQlJEQ0s2UVNPVmFUUzpmLUJhdjZPalRieThVRURSek5zeVZ3In0=

ℹ️ Configure other nodes to join this cluster:
• Copy the following enrollment token and start new Elasticsearch nodes with `bin/elasticsearch --enrollment-token <token>` (valid for the next 30 minutes):
  eyJ2ZXIiOiI4LjEyLjAiLCJhZHIiOlsiMTcyLjE4LjAuMjo5MjAwIl0sImZnciI6IjRlZjdkYjVmOGMxNDVhYWRlYWQ0YjA3ZTQ2NTE3NzYwZjk5ODI3NGEyZjM0ZmZjMjI5YmZjYTljOTU1MzllODMiLCJrZXkiOiI3UG9WZkkwQlJEQ0s2UVNPVmFUUzp1em1uUDd3SlIydVFfTHRBVVQwbG9BIn0=

  If you're running in Docker, copy the enrollment token and run:
  `docker run -e "ENROLLMENT_TOKEN=<token>" docker.elastic.co/elasticsearch/elasticsearch:8.12.0`
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


terminal: eliminar conexion segura
traemos el archivo del docker: sudo docker cp es01:/usr/share/elasticsearch/config/elasticsearch.yml .
lo editamos: todo lo que esta en true lo pasamos a false
enviamos el archivo al docker: sudo docker cp elasticsearch.yml es01:/usr/share/elasticsearch/config/elasticsearch.yml





- curl --cacert http_ca.crt -u elastic https://localhost:9200


KIBANA:
docker pull docker.elastic.co/kibana/kibana:8.12.0




	