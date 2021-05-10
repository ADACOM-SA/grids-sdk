package eu.grids.sdk.service.Impl;


import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import eu.grids.sdk.service.IGRIDSIssuer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class GRIDSIssuer implements IGRIDSIssuer {

    private URI issuerURI;
    private String clientId;
    private String clientSecret;
    private URI callbackURI;
    private OIDCProviderMetadata providerMetadata;


    public GRIDSIssuer(URI issuerURI, String clientId, String clientSecret, URI callbackURI) {
        this.issuerURI = issuerURI;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.callbackURI = callbackURI;
    }


    public OIDCProviderMetadata getOPMetadata() {


        URL providerConfigurationURL = null;
        try {
            providerConfigurationURL = issuerURI.resolve("/.well-known/openid-configuration").toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // TODO proper error handling
        }

        InputStream stream = null;
        try {
            stream = providerConfigurationURL.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO proper error handling
        }
        // Read all data from URL
        String providerInfo = null;
        try (java.util.Scanner s = new java.util.Scanner(stream)) {
            providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }

        OIDCProviderMetadata providerMetadata = null;
        try {
            providerMetadata = OIDCProviderMetadata.parse(providerInfo);
        } catch (ParseException e) {
            e.printStackTrace();
            // TODO proper error handling
        }

        return providerMetadata;
    }

    public String getAuthorizationUrl(OIDCClaimsRequest claims) {


        if (providerMetadata == null)
            this.getOPMetadata();

        // Generate random state string for pairing the response to the request
        State state = new State();
        // Generate nonce
        Nonce nonce = new Nonce();
        // Specify scope
        Scope scope = Scope.parse("openid");


        // Compose the request
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                providerMetadata.getAuthorizationEndpointURI(),
                new ResponseType(ResponseType.Value.CODE),
                scope, new ClientID(clientId), callbackURI, state, nonce);

        URI authReqURI = authenticationRequest.toURI();

        return authReqURI.toString();
    }

    public OIDCTokens requestToken(String callbackResponse) {

        if (providerMetadata == null)
            this.getOPMetadata();

        AuthorizationCode authCode = extractFromURL(callbackResponse);

        TokenRequest tokenReq = new TokenRequest(
                providerMetadata.getTokenEndpointURI(),
                new ClientSecretBasic(new ClientID(clientId),
                        new Secret(clientSecret)),

                new AuthorizationCodeGrant(authCode, callbackURI));

        HTTPResponse tokenHTTPResp = null;
        try {
            tokenHTTPResp = tokenReq.toHTTPRequest().send();
        } catch (SerializeException | IOException e) {
            // TODO proper error handling
        }

        // Parse and check response
        TokenResponse tokenResponse = null;

        try {
            tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        } catch (ParseException e) {
            // TODO proper error handling
        }

        if (tokenResponse instanceof TokenErrorResponse) {
            ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
            // TODO error handling
        }

        OIDCTokenResponse accessTokenResponse = (OIDCTokenResponse) tokenResponse;

        return accessTokenResponse.getOIDCTokens();

    }

    public UserInfo getUserInfo(String token) {

        if (providerMetadata == null)
            this.getOPMetadata();

        UserInfoRequest userInfoReq = new UserInfoRequest(
                providerMetadata.getUserInfoEndpointURI(),
                new BearerAccessToken(token));

        HTTPResponse userInfoHTTPResp = null;
        try {
            userInfoHTTPResp = userInfoReq.toHTTPRequest().send();
        } catch (SerializeException | IOException e) {
            // TODO proper error handling
        }

        UserInfoResponse userInfoResponse = null;

        try {
            userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
        } catch (ParseException e) {
            // TODO proper error handling
        }

        if (userInfoResponse instanceof UserInfoErrorResponse) {
            ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
            // TODO error handling
        }

        UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;
        return successResponse.getUserInfo();

    }

    public UserInfo getDPUserInfo(URI userInfoURI, String token) {

        if (providerMetadata == null)
            this.getOPMetadata();

        UserInfoRequest userInfoReq = new UserInfoRequest(
                userInfoURI,
                new BearerAccessToken(token));

        HTTPResponse userInfoHTTPResp = null;
        try {
            userInfoHTTPResp = userInfoReq.toHTTPRequest().send();
        } catch (SerializeException | IOException e) {
            // TODO proper error handling
        }

        UserInfoResponse userInfoResponse = null;

        try {
            userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
        } catch (ParseException e) {
            // TODO proper error handling
        }

        if (userInfoResponse instanceof UserInfoErrorResponse) {
            ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
            // TODO error handling
        }

        UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;
        return successResponse.getUserInfo();


    }


    private AuthorizationCode extractFromURL(String callbackUrl) {
        
        AuthenticationResponse authResp = null;
        try {
            authResp = AuthenticationResponseParser.parse(new URI(callbackUrl));
        } catch (ParseException | URISyntaxException e) {
            // TODO error handling
        }

        if (authResp instanceof AuthenticationErrorResponse) {
            ErrorObject error = ((AuthenticationErrorResponse) authResp)
                    .getErrorObject();
            // TODO error handling
        }

        AuthenticationSuccessResponse successResponse = (AuthenticationSuccessResponse) authResp;

        /* Don't forget to check the state!
         * The state in the received authentication response must match the state
         * specified in the previous outgoing authentication request.
         */
//        if (!verifyState(successResponse.getState())) {
//            // TODO proper error handling
//        }

        AuthorizationCode authCode = successResponse.getAuthorizationCode();

        return authCode;
    }


    public URI getIssuerURI() {
        return issuerURI;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public URI getCallbackURI() {
        return callbackURI;
    }

}
