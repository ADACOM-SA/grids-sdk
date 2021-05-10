package eu.grids.sdk;

import eu.grids.sdk.service.Impl.NetworkServiceImpl;
import eu.grids.sdk.service.NetworkService;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class NetworkServiceTest {

    private NetworkService networkService;

    private String accessToken;

    @Before
    public void init() {
        networkService = new NetworkServiceImpl();
        //Fake jwt
        accessToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    @Test
    public void testGet() throws NoSuchAlgorithmException, IOException, URISyntaxException {

        List<NameValuePair> requestParams = new ArrayList();
        requestParams.add(new BasicNameValuePair("foo1", "bar1"));

        String result = networkService.sendGet("https://postman-echo.com/", "get", accessToken, requestParams, 1);

        Assert.assertTrue(result.contains("\"authorization\":\"" + accessToken + "\""));

        Assert.assertTrue(result.contains("\"args\":{\"foo1\":\"bar1\"}"));

    }

    @Test
    public void testPostForm() throws NoSuchAlgorithmException, IOException {

        List<NameValuePair> requestParams = new ArrayList();
        requestParams.add(new BasicNameValuePair("foo1", "bar1"));
        requestParams.add(new BasicNameValuePair("foo2", "bar2"));

        String result = networkService.sendPostForm("https://postman-echo.com/", "post", accessToken, requestParams, 1);

        Assert.assertTrue(result.contains("\"authorization\":\"" + accessToken + "\""));
        Assert.assertTrue(result.contains("\"form\":{\"foo1\":\"bar1\",\"foo2\":\"bar2\"}"));

    }

    @Test
    public void testPostBody() throws IOException {

        class ObjectTest {
            private String foo1;
            private String foo2;

            private ObjectTest(String foo1, String foo2) {
                this.foo1 = foo1;
                this.foo2 = foo2;
            }

            public String getFoo1() {
                return foo1;
            }

            public void setFoo1(String foo1) {
                this.foo1 = foo1;
            }

            public String getFoo2() {
                return foo2;
            }

            public void setFoo2(String foo2) {
                this.foo2 = foo2;
            }
        }

        ObjectTest body = new ObjectTest("bar1", "bar2");

        String result = networkService.sendPostBody("https://postman-echo.com/", "post", accessToken, body, 1);

        Assert.assertTrue(result.contains("\"authorization\":\"" + accessToken + "\""));
        Assert.assertTrue(result.contains("\"data\":{\"foo1\":\"bar1\",\"foo2\":\"bar2\"}"));

    }


}
