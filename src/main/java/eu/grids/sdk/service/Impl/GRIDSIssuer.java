package eu.grids.sdk.service.Impl;


import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import eu.grids.sdk.service.IGRIDSIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;


public class GRIDSIssuer implements IGRIDSIssuer {

    Logger logger = LoggerFactory.getLogger(GRIDSIssuer.class);

    private URI issuerURI;
    private String clientId;
    private String clientSecret;
    private URI callbackURI;
    private OIDCProviderMetadata providerMetadata;
    private KeyPair jwksKeyPair;

    public GRIDSIssuer(URI issuerURI) {
        this.issuerURI = issuerURI;
    }

    public GRIDSIssuer(URI issuerURI, String clientId, String clientSecret, URI callbackURI,KeyPair jwksKeyPair) {
        this.issuerURI = issuerURI;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.callbackURI = callbackURI;
        this.jwksKeyPair = jwksKeyPair;
    }

    public OIDCProviderMetadata getOPMetadata() {


        URL providerConfigurationURL = null;
        try {
            String baseUrl = issuerURI.toString();
            providerConfigurationURL = new URL(new URL(baseUrl), ".well-known/openid-configuration");
        } catch (MalformedURLException e) {
            logger.error("Wrong issuerURI url format", e);
            return null;
        }

        InputStream stream = null;
        try {
            stream = providerConfigurationURL.openStream();
        } catch (IOException e) {
            logger.error("Could not retrieve Issuer metadata.", e);
            return null;
        }
        // Read all data from URL
        String providerInfo = null;
        try (java.util.Scanner s = new java.util.Scanner(stream)) {
            providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }

        try {
            providerMetadata = OIDCProviderMetadata.parse(providerInfo);
        } catch (ParseException e) {
            logger.error("Could not parse Issuer metadata.", e);
            return null;
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
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(providerMetadata.getAuthorizationEndpointURI(), new ResponseType(ResponseType.Value.CODE), (ResponseMode) null, scope, new ClientID(clientId), callbackURI, state, nonce, (Display) null, (Prompt) null, -1, (List) null, (List) null, (JWT) null, (String) null, (List) null, claims, (String) null, (JWT) null, (URI) null, (CodeChallenge) null, (CodeChallengeMethod) null, (List) null, false, (Map) null);

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
            logger.error("Could not retrieve token", e);
            return null;
        }

        // Parse and check response
        TokenResponse tokenResponse = null;

        try {
            tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        } catch (ParseException e) {
            logger.error("Could not parse token", e);
            return null;
        }

        if (tokenResponse instanceof TokenErrorResponse) {
            ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
            logger.error("Token error: " + error.getDescription());
            return null;
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
            logger.error("Could not retrieve userInfo", e);
            return null;
        }

        UserInfoResponse userInfoResponse = null;

        try {
            userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
        } catch (ParseException e) {
            logger.error("Could not parse userInfo", e);
            return null;
        }

        if (userInfoResponse instanceof UserInfoErrorResponse) {
            ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
            logger.error("UserInfo error: " + error.getDescription());
            return null;
        }

        UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;

        if (successResponse.getEntityContentType().matches(ContentType.APPLICATION_JSON)) {
            return successResponse.getUserInfo();
        } else {
            try {
                return new UserInfo(successResponse.getUserInfoJWT().getJWTClaimsSet());
            } catch (java.text.ParseException e) {
                logger.error("UserInfo JWT parse error.", e);

                e.printStackTrace();
                return null;
            }
        }

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
            logger.error("Could not retrieve DP userInfo", e);
            return null;
        }

        UserInfoResponse userInfoResponse = null;

        try {
            if (userInfoHTTPResp.getEntityContentType().equals(ContentType.APPLICATION_JWT)){

                JWT jwt = userInfoHTTPResp.getContentAsJWT();

                if (jwt instanceof EncryptedJWT) {
                    EncryptedJWT enryptedJwt = (EncryptedJWT) userInfoHTTPResp.getContentAsJWT();
                    enryptedJwt.decrypt(new RSADecrypter(jwksKeyPair.getPrivate()));
                    userInfoResponse = new UserInfoSuccessResponse(enryptedJwt);

                }else{
                    userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
                }

            }else{
                userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
            }


        } catch (ParseException e) {
            logger.error("Could not parse DP userInfo", e);
            return null;
        } catch (JOSEException e) {
            logger.error("Could not decrypt DP userInfo", e);

            e.printStackTrace();
            return null;
        }

        if (userInfoResponse instanceof UserInfoErrorResponse) {
            ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
            logger.error("DP UserInfo error: " + error.getDescription());
            return null;
        }

        UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;

        if (successResponse.getEntityContentType().matches(ContentType.APPLICATION_JSON)) {
            return successResponse.getUserInfo();
        } else {
            try {
                return new UserInfo(successResponse.getUserInfoJWT().getJWTClaimsSet());
            } catch (java.text.ParseException e) {
                logger.error("DP UserInfo JWT parse error.", e);
                return null;
            }
        }

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
            return null;
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

    public KeyPair getJwksKeyPair() {
        return jwksKeyPair;
    }

    public void setJwksKeyPair(KeyPair jwksKeyPair) {
        this.jwksKeyPair = jwksKeyPair;
    }
}
