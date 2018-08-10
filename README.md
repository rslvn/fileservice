
# docker
[![CircleCI](https://circleci.com/gh/rslvn/fileservice.svg?style=svg)](https://circleci.com/gh/rslvn/fileservice)
[![Build Status](https://travis-ci.com/rslvn/fileservice.svg?branch=master)](https://travis-ci.com/rslvn/fileservice)
[![Coverage Status](https://coveralls.io/repos/github/rslvn/fileservice/badge.svg?branch=master)](https://coveralls.io/github/rslvn/fileservice?branch=master)
## manage images and containers
```
docker-compose build
docker-compose down
docker-compose up -d
```

## container IPs
```
docker network inspect spring-fileservice-demo-project_default
```

## connect containers

```
~$ docker exec -it discovery /bin/sh
/ # tail -f logs/application.log 

```

```
~$ docker exec -it spring-fileservice-demo-project_server_1 /bin/sh
/ # tail -f logs/application.log 

```

```
~$ docker exec -it spring-fileservice-demo-project_client_1 /bin/sh
/ # tail -f logs/application.log 

```

# server

upload a file
```
curl -i -X POST -H "Content-Type: multipart/form-data" \
        -F "file=@test.out;filename=test.out" \
        http://192.168.80.3:8080/api/v1/files/upload
```

upload a file with folder structure
```
curl -i -X POST -H "Content-Type: multipart/form-data" \
        -F "file=@test/test.out;filename=test/test.out" \
        http://192.168.80.3:8080/api/v1/files/upload
```

download a file
```
curl -i -X GET \
        http://192.168.80.3:8080/api/v1/files/download/test.out
```

list files
```
# folder
curl -i -X GET -H "Content-Type: application/json" \
            "http://192.168.80.3:8080/api/v1/files/test"
# file            
curl -i -X GET -H "Content-Type: application/json" \
            "http://192.168.80.3:8080/api/v1/files/test.out"
```

delete file or folder
```
# file
curl -i -X DELETE \
        http://192.168.80.3:8080/api/v1/files/test.out/false
```


# client

upload a file
```
curl -i -X POST -H "Content-Type: multipart/form-data" \
        -F "file=@test.out;filename=test.out" \
        http://192.168.80.4:8090/api/v1/client/upload
```

upload a file with folder structure
```
curl -i -X POST -H "Content-Type: multipart/form-data" \
        -F "file=@test/test.out;filename=test/test.out" \
        http://192.168.80.4:8090/api/v1/client/upload
```

download a file
```
curl -i -X GET \
        http://192.168.80.4:8090/api/v1/client/download/test.out
```

list files
```
# folder
curl -i -X GET -H "Content-Type: application/json" \
            "http://192.168.80.4:8090/api/v1/client/test"
# file            
curl -i -X GET -H "Content-Type: application/json" \
            "http://192.168.80.4:8090/api/v1/client/test.out"
```

delete file or folder
```
# file
curl -i -X DELETE \
        http://192.168.80.4:8090/api/v1/client/test.out/false

#folder recursive
curl -i -X DELETE \
        http://192.168.80.4:8090/api/v1/client/test/true
        
#folder not recursive
curl -i -X DELETE \
        http://192.168.80.3:8080/api/v1/files/test/false

```