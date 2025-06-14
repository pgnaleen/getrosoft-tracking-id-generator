## Steps to run the app
### step 01
./gradlew bootJar

### step 02
#### copy build/libs/getrosoft-generate-id-0.0.1-SNAPSHOT.jar to deployment folder
cp ../build/libs/getrosoft-generate-id-0.0.1-SNAPSHOT.jar .

### step 03
sudo docker build -t tracking-app .

### step 04
docker run -p 8080:8080 tracking-app