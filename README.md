#  Facebook Replica Users Application
### Assigned User Stories:
Users should be able to:
 1. Sign up
 2. Login
 3. Delete Account
 4. Edit Profile
 5. Choose a cover photo and profile photo
 6. Send friend requests and receive requests
 7. Follow and unfollow users
 8. Block and unblock users
 9. Report users 

### In order to run the project, you will need to create 4 Docker images:
1.  Webserver1 which listens on port 8080.
2.  Webserver2 which listens on port 8081.
3.  HAProxy’s image which listens on port 80 (Load Balancer).
4.  UserApp image to create an instance from the UserApp microservice.

After you create the 4 Docker images, you will be ready to run the project using the `$ docker-compose up ` command.

1.  ### **Navigate to the project’s backend directory.**

**Create Webserver 1 Docker image**

```$ docker build -t facebook-replica-webserver1 . -f Webserver1.Dockerfile```

**Create Webserver 2 Docker image**

```$ docker build -t facebook-replica-webserver2 . -f Webserver2.Dockerfile```

**Create HAProxy Docker image**

```$ docker build -t facebook-replica-loadbalancer . -f HAProxy.Dockerfile```

**Create UserApp Docker image**

```$ docker build -t facebook-replica-userapp . -f UserApp.Dockerfile```

2. ### **Run the docker-compose.yml file**

```$ docker-compose up --build```

You will now be able to send and receive HTTP requests through postman on:   **http://\<docker-ip\>:80**

The following command gets the docker IP address

> `$ docker-machine ip default`

**Note: The default docker IP address is 192.168.99.100**

The databases contain dummy insertions so that you can test easily.

## Technologies Used:
1. RabbitMQ
2. PostgreSQL
3. ArangoDB
4. MinIO
5. Redis
6. HAProxy
7. Docker
