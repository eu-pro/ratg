import freemarker.cache.FileTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus

/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

class FileGenerator {

    private static final String testName = "TestRecording"
    private static final String endpoint = "Endpoint"
    private static final String request = "Request"
    private static final String response = "Response"
    private static final String generatedDir = "$rootDir/src/test/groovy/generated"
    private static final String templatesDir = "$rootDir/src/templates"

    //freemarker static configuration
    private static Configuration config
    static {
        config = new Configuration()
        config.setTemplateLoader(new FileTemplateLoader(
                new File(templatesDir),
                true
        ))
        config.setDefaultEncoding("UTF-8")
        config.setLocale(Locale.US)
    }

    static void generateTestClassFiles(ArrayList<Object> requestResponseArray) {

        String className = testName
        ArrayList<HashMap<String, String>> endpointsData = []

        requestResponseArray.each {
            HttpMethod httpMethod = it?.method
            String methodType = httpMethod.toString()
            String uri = it.uri as String
            Map<String, Object> requestHeadersMap = it.requestHeadersMap as HashMap<String, Object>
            String requestJsonString = it.requestJson as String
            Map<String, Object> responseHeadersMap = it.responseHeadersMap as HashMap<String, Object>
            String responseJsonString = it.responseJson as String
            HttpResponseStatus httpResponseStatus = it.status as HttpResponseStatus
            String generatedClassName = GeneratorHelper.generateClassNameFromUri(uri)

            //remove headers
            requestHeadersMap.remove("Content-Length")
            requestHeadersMap.remove("host")

            endpointsData << [
                    'uri'               : uri,
                    'method'            : methodType,
                    'requestHeadersMap' : GeneratorHelper.mapToStringMap(requestHeadersMap),
                    'responseHeadersMap': GeneratorHelper.mapToStringMap(responseHeadersMap),
                    'responseBodyMap'   : GeneratorHelper.jsonToStringMap(responseJsonString),
                    'status'            : httpResponseStatus.toString(),
                    'endpointClassName' : "$generatedClassName$endpoint$methodType",
            ]

            //if httpMethod != HttpMethod.GET process request body
            if (httpMethod != HttpMethod.GET) {
                endpointsData.last() << ['requestBodyMap': GeneratorHelper.jsonToStringMap(requestJsonString)]
                generateGroovyDtoClass("$generatedClassName$request$methodType", requestJsonString)
            }
            generateGroovyDtoClass("$generatedClassName$response$methodType", responseJsonString)
            generateEndpointClassFile(httpMethod, generatedClassName)
        }

        HashMap<String, Object> root = ["className": className, "endpointsData": endpointsData]
        writeClassFile("recordTest.ftl", root, className)
    }

    private static void generateGroovyDtoClass(String className, String json) {
        Map<String, Object> map = GeneratorHelper.jsonStringToMap(json)
        Map<String, Object> root = ["сlassName": className, "rows": ClassDtoGenerator.groovyClassDto(map)]
        writeClassFile("groovyClassDto.ftl", root, className)
    }

    private static void generateEndpointClassFile(HttpMethod httpMethod, String className) {
        String httpMethodStr = httpMethod.toString()
        String endpointClassName = "${className}$endpoint$httpMethodStr"
        HashMap<String, Object> root = [
                "endpointClassName": endpointClassName,
                "httpMethod"       : httpMethodStr,
                "requestClassName" : "${GeneratorHelper.firstCharacterCase(className)}$request$httpMethodStr",
                "requestClassVar"  : "${GeneratorHelper.firstCharacterCase(className, false)}$request$httpMethodStr",
                "responseClassName": "${GeneratorHelper.firstCharacterCase(className)}$response$httpMethodStr",
                "responseClassVar" : "${GeneratorHelper.firstCharacterCase(className, false)}$response$httpMethodStr"
        ]
        writeClassFile("endpoint.ftl", root, endpointClassName)
    }

    private static writeClassFile(String templateName, HashMap<String, Object> root, String className) {
        Template template = config.getTemplate(templateName)
        template.process(root, new OutputStreamWriter(System.out))

        Writer fileWriter = new FileWriter(new File(generatedDir, className + ".groovy"))
        try {
            template.process(root, fileWriter)
        } finally {
            fileWriter.close()
        }
    }

    private static String getRootDir() {
        System.getProperty("user.dir")
    }
}