package generated

import helper.BaseRestGroovyTest
import io.netty.handler.codec.http.HttpMethod

class ${endpointClassName} extends BaseRestGroovyTest {

    <#if httpMethod != 'GET'>
    ${requestClassName} ${requestClassVar} = new ${requestClassName}()
    </#if>
    ${responseClassName} ${responseClassVar} = new ${responseClassName}()

    ${endpointClassName}(String endpoint, HttpMethod httpMethod = HttpMethod.${httpMethod}) {
        super(endpoint, httpMethod)
    }
}