import groovy.json.JsonSlurper

/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

class GeneratorHelper {

    static String generateClassNameFromUri(String uri) {
        char[] charArray = uri.toCharArray()
        Integer[] list = charArray.findIndexValues { it == '/' }
        //remove '/'
        list.each {
            int index = it.toInteger() + 1
            charArray[index] = uri.toCharArray()[index].toUpperCase()
        }
        charArray.toString().replaceAll("/", "")
    }

    static String firstCharacterCase(String str, boolean toUpperCase = true) {
        char[] charArray = str.toCharArray()
        charArray[0] = toUpperCase ? charArray[0].toUpperCase() : charArray[0].toLowerCase()
        charArray.toString()
    }

    static Map<String, Object> jsonStringToMap(String json) {
        new JsonSlurper().parseText(json) as Map<String, Object>
    }

    static String jsonToStringMap(String json) {
        toMap(jsonStringToMap(json)).toString()
    }

    static String mapToStringMap(Map<String, Object> map) {
        toMap(map).toString()
    }

    @SuppressWarnings("all")
    private static Map<String, Object> toMap(Map<String, Object> inputMap) {
        Map<String, Object> map = [:]
        inputMap.keySet().each {
            Object value = inputMap.get(it);
            if (value instanceof List) {
                value = toList(value as List);
            } else if (value instanceof Map) {
                value = toMap(value as Map);
            }

            if (value instanceof String) {
                map << ["\"$it\"": "\"$value\""]
            } else if (value instanceof ArrayList && value[0] instanceof String) {
                def toListString = { ArrayList<String> arrayList ->
                    String output = ""
                    arrayList.eachWithIndex { String entry, int index ->
                        output += "\"${entry}\"${(arrayList.size() != index + 1) ? "," : ""}"
                    }
                    output
                }
                map << ["\"$it\"": "[${toListString(value)}] "]
            } else {
                map << ["\"$it\"": "$value"]
            }

        }
        map
    }

    private static List<Object> toList(List array) {
        List<Object> list = []
        array.each {
            if (it instanceof List) {
                it = toList(it as List)
            } else if (it instanceof Map) {
                it = toMap(it as Map)
            }
            list << it
        }
        list
    }
}