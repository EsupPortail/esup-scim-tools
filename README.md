# ESUP-SCIM-PROXY

## Description

ESUP-SCIM-TOOLS is 
* a Java application that runs as a SCIM server and memorizes users and groups in memory
* a SCIM client that connects to a SCIM server and generate / provides some groups and users to put in the SCIM server.

It is based on the [Spring Boot](https://spring.io/projects/spring-boot) framework and uses Apache SCIMple as a SCIM library.

## Features

The SCIM server part has been tested with :
* Grouper 5 of Internet2 as a SCIM client
* the SCIM client from this project itself.

## Configuration

The configuration is done in the `application.properties` file : you can simply modify username and password for the SCIM server and client.

