# ratg
# REST API Test Generator

The framework generates Rest API tests written in Groovy with REST Assured and TestNG automatically.
For instance, it can be useful when covering end-to-end tests. It is just needed to specify test server address, this framework server address and to send requests from client app.

Generated test example:
```
   @Test
    void "Test Recording"() {

        // GET /snippets; http response status: 200 
        new SnippetsEndpointGET("/snippets").executeAndValidate(
                ["TestHeader":"TestHeaderValue", "Accept":"*/*", "Accept-Encoding":"gzip, deflate, br"],
                [:],
                ["Content-Type":"application/json;charset=UTF-8", "Content-Length":"290"],
                ["snippets":[["id":"AAAA-AAAA-AAAA-AAAA", "count":111], ["id":"AAAA-AAAA-AAAA-AAAA", "count":111], ["id":"AAAA-AAAA-AAAA-AAAA", "count":111], ["id":"AAAA-AAAA-AAAA-AAAA", "count":111]]])

        // PUT /snippets/complicated; http response status: 200 
        new SnippetsComplicatedEndpointPUT("/snippets/complicated").executeAndValidate(
                ["Content-Type":"application/json", "Accept":"*/*", "Accept-Encoding":"gzip, deflate, br"],
                ["id":"PUTT-PUTT-PUTT-PUTT", "count":111],
                ["Content-Type":"application/json;charset=UTF-8", "Content-Length":"243"],
                ["level":"1", "count":100, "enabled":true, "secondLelel":["level":"2", "count":200, "enabled":false, "thirdLevel":["level":"3", "count":300, "enabled":true, "str":"str"]]])

        // POST /snippets/complicated; http response status: 200 
        new SnippetsComplicatedEndpointPOST("/snippets/complicated").executeAndValidate(
                ["Content-Type":"application/json", "Accept":"*/*", "Accept-Encoding":"gzip, deflate"],
                ["id":"POST-POST-POST-POST", "count":111],
                ["Content-Type":"application/json;charset=UTF-8", "Content-Length":"728"],
                ["level":"1", "count":100, "enabled":true, "firstLevelList":["str1","str2"] , "secondLevel":["level":"2", "count":200, "enabled":false, "secondLevelList":["str1","str2"] , "thirdLevel":["level":"3", "count":300, "enabled":true, "thirdLevellList":["str1","str2"] ]], "lists":[["first":"list1", "second":["00","01","02"] ], ["first":"list2", "second":["10","11","12"] ], ["first":"list3", "second":["20","21","22"] ]]])
    }
```


By default, the response is validated by comparing the maps, but since Rest Assured is used, all of its capabilities can be used.
To add some features BaseRestGroovyTest can be extended, if necessary.

```
import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.equalTo
    ...
    @Test
    void "Test"() {
        // POST /snippets/complicated; http response status: 200
        Map map = new SnippetsComplicatedEndpointPOST("/snippets/complicated").executeAndValidate(
                ["Content-Type": "application/json", "Accept": "*/*", "Accept-Encoding": "gzip, deflate, br"],
                ["id": "POST-POST-POST-POST", "count": 111],
                {
                    statusCode(HttpStatus.SC_OK)
                    header("Content-Type", containsString("application/json"))
                    body("level", equalTo("1"))
                    body("secondLevel.secondLevelList[0]", equalTo("str1"))
                    //etc
                }
        ).extract().as(HashMap)

        assert map.level == "1"                             //true
        assert map.secondLevel.secondLevelList[0] == "str1" //true
    }
```


Also, this framework automatically generates data classes that can also be useful for reuse in a test.
For instance, orginal json response:
```
	{
	  "valid": true,
	  "id": "AAAA-AAAA-AAAA-AAAA",
	  "count": 100
	}
```	
is converted to data class:
```

	package generated

	class SnippetsPostResponsePOST {
		Boolean valid
		Integer count
		String id
	}
```

Example with the data class when type casting is used for auto generated class (e.g. extract().as(generatedDataClass)):
```

    @Test
    void "Test"() {
        // POST /snippets/post; http response status: 200 
        SnippetsPostResponsePOST snippetsPostResponsePOST = new SnippetsPostEndpointPOST("/snippets/post").executeAndValidate(
                ["Content-Type": "application/json", "Accept": "*/*", "Accept-Encoding": "gzip, deflate, br"],
                ["id": "AAAA-AAAA-AAAA-AAAA", "count": 111],
                {
                    body("valid", equalTo(true))
                    body("id", equalTo("AAAA-AAAA-AAAA-AAAA"))
                    body("count", equalTo(100))
                }).extract().as(SnippetsPostResponsePOST)

        assert snippetsPostResponsePOST.valid == true               //true
        assert snippetsPostResponsePOST.id == "AAAA-AAAA-AAAA-AAAA" //true
        assert snippetsPostResponsePOST.count == 100                //true
    }
```    

Endpoint (e.g. SnippetsComplicatedEndpointPOST("/snippets/complicated")) can return maps or generated classes based on json response:

Also the framework generates data class based on json request. For instance, generated classes:
```
generated.SnippetsComplicatedEndpointPOST
generated.SnippetsComplicatedEndpointPUT
generated.SnippetsComplicatedRequestPOST
generated.SnippetsComplicatedRequestPUT
generated.SnippetsComplicatedResponsePOST
generated.SnippetsComplicatedResponsePUT
generated.SnippetsEndpointGET
generated.SnippetsPostEndpointPOST
generated.SnippetsPostRequestPOST
generated.SnippetsPostResponsePOST
generated.SnippetsResponseGET
...
generated.TestRecording
```

Since with this approach there is no need to manually compose requests, validate responses, analyze server logs etc, this can significantly reduce the time for tests developing and support.
With these automatically generated data classes it is possible to update tests fast and in convenient way.

Addresses for test server url and proxy server url are located in ProxyServer class and test server url in BaseRestGroovyTest class.
Generated files are located in test/groovy/generated.
