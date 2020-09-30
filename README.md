# Akka Performance Measuring

## Usage

### Build

```shell script
./build-docker-image
```

### Run
1. Build docker.
2. docker run.
3. docker exec.

#### Run with predefined test input

```shell script
java -jar app.jar --scriptMode < test.in.tx
```

#### Run with REPL
```shell script
java -jar app.jar
```

## Inspire by
[plokhotnyuk/actors](https://github.com/plokhotnyuk/actors)