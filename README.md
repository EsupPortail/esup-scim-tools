# ESUP-SCIM-PROXY

## Description

ESUP-SCIM-PROXY is a Java application that runs as a proxy between a SCIM client and a SCIM server.

It is based on the [Spring Boot](https://spring.io/projects/spring-boot) framework and uses Apache SCIMple as a SCIM library.

## Features

You can use ESUP-SCIM-PROXY to:
* display requests and responses between the SCIM client and the SCIM server
* or mock the SCIM server part to test the SCIM client part : users and groups are are generated on the fly, stored in memory and not persisted.

It is developed using Grouper 5 of Internet2 as a SCIM client.

## Configuration

The configuration is done in the `application.properties` file.

