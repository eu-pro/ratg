package generated

import helper.BaseRestGroovyTest
import io.netty.handler.codec.http.HttpMethod

class SnippetsPostEndpointPOST extends BaseRestGroovyTest {

    SnippetsPostRequestPOST snippetsPostRequestPOST = new SnippetsPostRequestPOST()
    SnippetsPostResponsePOST snippetsPostResponsePOST = new SnippetsPostResponsePOST()

    SnippetsPostEndpointPOST(String endpoint, HttpMethod httpMethod = HttpMethod.POST) {
        super(endpoint, httpMethod)
    }
}