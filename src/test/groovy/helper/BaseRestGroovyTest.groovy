package helper

import groovy.json.JsonSlurper
import io.netty.handler.codec.http.HttpMethod
import io.restassured.RestAssured
import io.restassured.response.ExtractableResponse
import io.restassured.response.ValidatableResponse
import io.restassured.response.ValidatableResponseOptions
import io.restassured.specification.RequestSpecification

/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

class BaseRestGroovyTest extends RestAssured {

    protected String endpoint
    protected static final String testServerUrl = "http://127.0.0.1:8087"

    protected HttpMethod httpMethod

    BaseRestGroovyTest(String endpoint, HttpMethod httpMethod) {
        this.endpoint = endpoint
        this.httpMethod = httpMethod
    }

    Map<String, Object> execute(Map<String, Object> headers, Map<String, Object> body) {
        Map<String, Object> response = executeEndpoint(headers, body)
                .extract()
                .body()
                .as(HashMap)
        response
    }

    Map<String, Object> execute(Map<String, Object> headers, String body) {
        Map<String, Object> response = executeEndpoint(headers, new JsonSlurper().parseText(body) as Map<String, Object>)
                .extract()
                .body()
                .as(HashMap)
        response
    }

    Map<String, Object> executeAndValidate(
            HashMap<String, String> requestHeadersMap,
            HashMap<String, Object> requestBodyMap,
            HashMap<String, String> responseHeadersMap,
            HashMap<String, Object> responseBodyMap
    ) {
        ExtractableResponse response = executeEndpoint(requestHeadersMap, requestBodyMap).extract()
        assert response.body().as(Map) == responseBodyMap
        response.body().as(HashMap)
    }

    Map<String, Object> executeAndValidate(
            HashMap<String, String> requestHeadersMap,
            String body,
            HashMap<String, String> responseHeadersMap,
            HashMap<String, Object> responseBodyMap
    ) {
        Map<String, Object> requestBodyMap = new JsonSlurper().parseText(body) as Map<String, Object>
        ExtractableResponse response = executeEndpoint(requestHeadersMap, requestBodyMap).extract()
        assert response.body().as(Map) == responseBodyMap
        response.body().as(HashMap)
    }

    Map<String, Object> executeAndValidate(
            HashMap<String, String> requestHeadersMap,
            HashMap<String, Object> requestBodyMap
    ) {
        ExtractableResponse response = executeEndpoint(requestHeadersMap, requestBodyMap).extract()
        response.body().as(HashMap)
    }

    ValidatableResponse executeAndValidate(
            Map<String, Object> headers,
            Map<String, Object> body,
            @DelegatesTo(ValidatableResponseOptions) Closure verify = null
    ) {
        ValidatableResponse response = executeEndpoint(headers, body) as ValidatableResponse
        if (verify) {
            verify.delegate = response
            verify.call()
        }
        response
    }

    //etc

    @SuppressWarnings('GroovyVariableNotAssigned')
    private def executeEndpoint(
            Map<String, Object> requestHeadersMap,
            Map<String, Object> requestBodyMap
    ) {
        RequestSpecification requestSpec = given().baseUri(testServerUrl).headers(requestHeadersMap).when().log().all()
        ValidatableResponseOptions validatableResponseOptions
        switch (httpMethod) {
            case HttpMethod.GET:
                validatableResponseOptions = requestSpec.get(endpoint).then()
                break
            case HttpMethod.PUT:
                validatableResponseOptions = requestSpec.body(requestBodyMap).put(endpoint).then()
                break
            case HttpMethod.POST:
                validatableResponseOptions = requestSpec.body(requestBodyMap).post(endpoint).then()
                break
            case HttpMethod.DELETE:
                validatableResponseOptions = requestSpec.body(requestBodyMap).delete(endpoint).then()
                break
        }
        validatableResponseOptions.log().all()
    }
}