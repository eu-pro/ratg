
/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

class ClassDtoGenerator {

    private enum ClassType {
        Object("Object"), String("String"), UUID("UUID"), Boolean("Boolean"),
        Integer("Integer"), BigDecimal("BigDecimal"), ArrayList("ArrayList")
        String name

        ClassType(String name) {
            this.name = name
        }
    }

    static ArrayList<String> groovyClassDto(
            Map<String, Object> map,
            ArrayList<String> generatedClassRows = new ArrayList<>()
    ) {
        Map<String, HashMap<String, Object>> nestedClasses = new HashMap<>()
        Map<String, String> fields = new HashMap<>()

        map.keySet().each {
            if (map.get(it) instanceof Map) {
                Map<String, Object> tempMap = map.get(it) as HashMap
                if (tempMap) {
                    nestedClasses.put(it.toString(), tempMap as HashMap)
                    String className = GeneratorHelper.firstCharacterCase(it.toString())
                    String objectName = GeneratorHelper.firstCharacterCase(it.toString(), false)
                    generatedClassRows.add("$className $objectName = new ${className}()")
                }
            } else {
                ClassType fieldClassType = defineObjectType(map.get(it))
                if (fieldClassType == ClassType.ArrayList) {
                    ArrayList arrayList = map.get(it) as ArrayList

                    if (arrayList[0] instanceof Map) {
                        nestedClasses.put(it.toString(), arrayList[0] as HashMap)

                        String className = GeneratorHelper.firstCharacterCase(it.toString())
                        String objectName = GeneratorHelper.firstCharacterCase(it.toString(), false)

                        generatedClassRows.add("ArrayList<$className> ${objectName}List = new ArrayList<>()")
                    } else {
                        fields.put(it?.toString(), "ArrayList<${defineObjectType(arrayList[0]).name}> ")
                    }
                } else {
                    fields.put(it.toString(), fieldClassType.name)
                }
            }
        }

        fields.each {
            generatedClassRows.add("$it.value $it.key")
        }

        if (nestedClasses.size()) {
            nestedClasses.each {
                String className = GeneratorHelper.firstCharacterCase(it.key)
                generatedClassRows.add("static class $className {")
                groovyClassDto(nestedClasses.get(it.key), generatedClassRows)
                generatedClassRows.add("}")
            }
        }
        generatedClassRows
    }

    private static ClassType defineObjectType(Object o) {
        switch (o) {
            case String.class:
                try {
                    if (UUID.fromString(o.toString()).toString() == o.toString()) {
                        return ClassType.UUID
                    }
                } finally {
                    return ClassType.String
                }
                return ClassType.String
                break
            case Boolean.class:
                return ClassType.Boolean
                break
            case Integer.class:
                return ClassType.Integer
                break
            case BigDecimal.class:
                return ClassType.BigDecimal
                break
            case ArrayList.class:
                return ClassType.ArrayList
            default:
                ClassType.Object
                break
        }
    }
}