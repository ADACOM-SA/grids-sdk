package eu.grids.sdk.service.Impl;

import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.client.*;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import eu.grids.sdk.service.IGRIDSClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

public class GRIDSClientManager implements IGRIDSClientManager {

    Logger logger = LoggerFactory.getLogger(GRIDSClientManager.class);

    private final URI clientsEndpoint;

    public GRIDSClientManager(URI clientsEndpoint) {
        this.clientsEndpoint = clientsEndpoint;
    }

    public ClientInformation registerClient(OIDCClientMetadata clientMetadata, String masterToken) throws IOException, ParseException {

        BearerAccessToken token = new BearerAccessToken(masterToken);

        clientMetadata.setGrantTypes(Collections.singleton(GrantType.AUTHORIZATION_CODE));

        ClientRegistrationRequest regRequest = new ClientRegistrationRequest(
                clientsEndpoint,
                clientMetadata,
                token
        );

        HTTPResponse httpResponse = regRequest.toHTTPRequest().send();

        ClientRegistrationResponse regResponse = ClientRegistrationResponse.parse(httpResponse);

        if (!regResponse.indicatesSuccess()) {
            ClientRegistrationErrorResponse errorResponse = (ClientRegistrationErrorResponse) regResponse;
            logger.error("Client registration error: " + errorResponse.getErrorObject().getDescription());
            return null;
        }

        // Successful registration
        ClientInformationResponse successResponse = (ClientInformationResponse) regResponse;
        return successResponse.getClientInformation();

    }

    @Override
    public ClientInformation getRegisteredClientInformation(URI registrationURI, String registrationAccessToken) throws IOException, ParseException {

        ClientReadRequest readRequest = new ClientReadRequest(
                registrationURI,
                new BearerAccessToken(registrationAccessToken)

        );

        HTTPResponse httpResponse = readRequest.toHTTPRequest().send();

        ClientRegistrationResponse regResponse = ClientRegistrationResponse.parse(httpResponse);

        if (!regResponse.indicatesSuccess()) {
            ClientRegistrationErrorResponse errorResponse = (ClientRegistrationErrorResponse) regResponse;
            logger.error("Get client information error: " + errorResponse.getErrorObject().getDescription());
            return null;
        }

        ClientInformationResponse successResponse = (ClientInformationResponse) regResponse;
        return successResponse.getClientInformation();

    }

    @Override
    public ClientInformation updateRegisteredClient(ClientInformation clientInformation) throws IOException, ParseException {

        ClientUpdateRequest updateRequest = new ClientUpdateRequest(
                clientInformation.getRegistrationURI(),
                clientInformation.getID(),
                clientInformation.getRegistrationAccessToken(),
                clientInformation.getMetadata(),
                clientInformation.getSecret()
        );

        HTTPResponse httpResponse = updateRequest.toHTTPRequest().send();

        ClientRegistrationResponse regResponse = ClientRegistrationResponse.parse(httpResponse);

        if (! regResponse.indicatesSuccess()) {
            ClientRegistrationErrorResponse errorResponse = (ClientRegistrationErrorResponse)regResponse;
            logger.error("Update client information error: " + errorResponse.getErrorObject().getDescription());
            return null;
        }


        ClientInformationResponse successResponse = (ClientInformationResponse) regResponse;
        return successResponse.getClientInformation();

    }

    @Override
    public Boolean deleteRegisteredClient(URI registrationURI, String registrationAccessToken) throws IOException, ParseException {

        ClientDeleteRequest deleteRequest = new ClientDeleteRequest(
                registrationURI,
                new BearerAccessToken(registrationAccessToken)
        );

        HTTPResponse httpResponse = deleteRequest.toHTTPRequest().send();

        if (! httpResponse.indicatesSuccess()) {
            logger.error("Delete client error: " + httpResponse.getContentAsJSONObject());
            return false;
        }

        return true;
    }


    public URI getClientsEndpoint() {
        return clientsEndpoint;
    }
}
