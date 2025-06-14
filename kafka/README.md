# kafka installation

### URL
https://hub.docker.com/r/apache/kafka

### Docker Commands
#### step 01: download image and run kafka
docker run -d --name broker apache/kafka:latest

#### step 02: connect to the kafka server
docker exec --workdir /opt/kafka/bin/ -it broker sh

#### step 3: create topic
./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test-topic



