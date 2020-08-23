package data

import FileGenerator
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus

/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

class TestData {
    companion object {
        val instance: TestData by lazy { TestData() }
    }

    private var requestResponseArray: ArrayList<Any> = ArrayList()
    private var currentRequestResponse = mutableMapOf<Any, Any>()

    fun setRequestData(method: HttpMethod, uri: String, requestHeadersMap: Map<String, String>, requestJson: String) {
        currentRequestResponse.putAll(mapOf(
                "method" to method,
                "uri" to uri,
                "requestHeadersMap" to requestHeadersMap,
                "requestJson" to requestJson
        ))
    }

    fun setResponseData(responseHeadersMap: Map<String, String>, responseJson: String, status: HttpResponseStatus) {
        currentRequestResponse.putAll(mapOf(
                "responseHeadersMap" to responseHeadersMap,
                "responseJson" to responseJson,
                "status" to status
        ))
        generateTestClasses()
    }

    private fun generateTestClasses() {
        requestResponseArray.add((currentRequestResponse as LinkedHashMap).clone())
        currentRequestResponse.clear()
        FileGenerator.generateTestClassFiles(requestResponseArray)
    }
}

