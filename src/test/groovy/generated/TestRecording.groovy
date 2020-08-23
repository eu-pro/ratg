package generated

import org.testng.annotations.Test

class TestRecording {

    @Test
    void "Test Recording"() {
        // POST /snippets/post; http response status: 200 
        new SnippetsPostEndpointPOST("/snippets/post").executeAndValidate(
                ["Content-Type": "application/json", "Accept": "*/*", "Accept-Encoding": "gzip, deflate, br"],
                ["id": "AAAA-AAAA-AAAA-AAAA", "count": 111],
                ["Content-Type": "application/json;charset=UTF-8"],
                ["valid": true, "id": "AAAA-AAAA-AAAA-AAAA", "count": 111])

    }
}