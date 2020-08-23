package generated

import org.testng.annotations.Test

class ${className} {

    @Test
    void "Test Recording"() {
        <#list endpointsData as endpoint>
        // ${endpoint['method']} ${endpoint['uri']}; http response status: ${endpoint['status']}
        new ${endpoint['endpointClassName']}("${endpoint['uri']}").executeAndValidate(
                ${endpoint['requestHeadersMap']},
                <#if endpoint['method'] != 'GET'>
                ${endpoint['requestBodyMap']},
                <#else>
                [:],
                </#if>
                ${endpoint['responseHeadersMap']},
                ${endpoint['responseBodyMap']})

        </#list>
    }
}