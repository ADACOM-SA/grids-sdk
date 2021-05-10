package eu.grids.sdk.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;

public interface NetworkService {
    public String sendGet(String hostUrl, String uri, String authorization, List<NameValuePair> urlParameters, int attempt) throws IOException, NoSuchAlgorithmException, URISyntaxException;

    public String sendPostForm(String hostUrl, String uri, String authorization, List<NameValuePair> urlParameters, int attempt) throws IOException, NoSuchAlgorithmException;

    public String sendPostBody(String hostUrl, String uri, String authorization, Object postBody, int attempt) throws IOException, ClientProtocolException;
}
