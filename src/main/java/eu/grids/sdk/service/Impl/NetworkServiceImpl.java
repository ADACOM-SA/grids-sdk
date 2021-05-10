package eu.grids.sdk.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.grids.sdk.service.NetworkService;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

public class NetworkServiceImpl implements NetworkService {

    private final static Logger LOG = Logger.getLogger(NetworkServiceImpl.class.getName());

    public NetworkServiceImpl() {

    }

    public String sendPostBody(String hostUrl, String uri, String authorization, Object postBody, int attempt) throws IOException, ClientProtocolException {

        String result = "";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            HttpPost request = new HttpPost(hostUrl + uri);

            // add request headers
            if (authorization != null) {
                request.addHeader("authorization", authorization);
            }
            request.addHeader("original-date", getNowDateFormatted());
            request.addHeader("content-type", "application/json");


            // send a JSON data
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(postBody);
            request.setEntity(new StringEntity(json));

            CloseableHttpResponse response = httpClient.execute(request);

            try {


                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    result = EntityUtils.toString(entity);

                }

            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }

        return result;

    }

    public String sendPostForm(String hostUrl, String uri, String authorization, List<NameValuePair> urlParameters, int attempt) throws IOException, NoSuchAlgorithmException {


        String result = "";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            HttpPost request = new HttpPost(hostUrl + uri);

            // add request headers
            if (authorization != null) {
                request.addHeader("authorization", authorization);
            }
            request.addHeader("original-date", getNowDateFormatted());
            request.addHeader("content-type", "application/x-www-form-urlencoded");


            // send a UrlEncodedForm
            request.setEntity(new UrlEncodedFormEntity(urlParameters));

            CloseableHttpResponse response = httpClient.execute(request);

            try {

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    result = EntityUtils.toString(entity);

                }

            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }

        return result;

    }

    public String sendGet(String hostUrl, String uri, String authorization, List<NameValuePair> urlParameters, int attempt) throws IOException, NoSuchAlgorithmException, URISyntaxException {

        String result = "";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            URIBuilder uriBuilder = new URIBuilder(hostUrl + uri);
            if (urlParameters != null) {
                uriBuilder.addParameters(urlParameters);
            }


            HttpGet request = new HttpGet(uriBuilder.build());

            // add request headers
            if (authorization != null) {
                request.addHeader("authorization", authorization);
            }
            request.addHeader("original-date", getNowDateFormatted());

            CloseableHttpResponse response = httpClient.execute(request);

            try {

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    result = EntityUtils.toString(entity);

                }

            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }

        return result;

    }

    private String getNowDateFormatted() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }

}
