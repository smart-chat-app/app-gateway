# Read Me First
The following was discovered as part of building this project:

* No Docker Compose services found. As of now, the application won't start! Please add at least one service to the `compose.yaml` file.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.4/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.4/maven-plugin/build-image.html)
* [Spring Boot Testcontainers support](https://docs.spring.io/spring-boot/3.5.4/reference/testing/testcontainers.html#testing.testcontainers)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/3.5.4/specification/configuration-metadata/annotation-processor.html)
* [Docker Compose Support](https://docs.spring.io/spring-boot/3.5.4/reference/features/dev-services.html#features.dev-services.docker-compose)
* [Spring Reactive Web](https://docs.spring.io/spring-boot/3.5.4/reference/web/reactive.html)
* [Rest Repositories](https://docs.spring.io/spring-boot/3.5.4/how-to/data-access.html#howto.data-access.exposing-spring-data-repositories-as-rest)
* [Spring Session for Spring Data Redis](https://docs.spring.io/spring-session/reference/)
* [Spring Security](https://docs.spring.io/spring-boot/3.5.4/reference/web/spring-security.html)
* [OAuth2 Client](https://docs.spring.io/spring-boot/3.5.4/reference/web/spring-security.html#web.security.oauth2.client)
* [OAuth2 Authorization Server](https://docs.spring.io/spring-boot/3.5.4/reference/web/spring-security.html#web.security.oauth2.authorization-server)
* [OAuth2 Resource Server](https://docs.spring.io/spring-boot/3.5.4/reference/web/spring-security.html#web.security.oauth2.server)
* [WebSocket](https://docs.spring.io/spring-boot/3.5.4/reference/messaging/websockets.html)
* [Testcontainers](https://java.testcontainers.org/)
* [Apache Zookeeper Configuration](https://docs.spring.io/spring-cloud-zookeeper/reference/config.html)
* [Gateway](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-mvc.html)
* [Reactive Gateway](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a Reactive RESTful Web Service](https://spring.io/guides/gs/reactive-rest-service/)
* [Accessing JPA Data with REST](https://spring.io/guides/gs/accessing-data-rest/)
* [Accessing Neo4j Data with REST](https://spring.io/guides/gs/accessing-neo4j-data-rest/)
* [Accessing MongoDB Data with REST](https://spring.io/guides/gs/accessing-mongodb-data-rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Using WebSocket to build an interactive web application](https://spring.io/guides/gs/messaging-stomp-websocket/)
* [Using Spring Cloud Gateway](https://github.com/spring-cloud-samples/spring-cloud-gateway-sample)

### Docker Compose support
This project contains a Docker Compose file named `compose.yaml`.

However, no services were found. As of now, the application won't start!

Please make sure to add at least one service in the `compose.yaml` file.

### Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/3.5.4/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following Docker images:


Please review the tags of the used images and set them to the same as you're running in production.

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

