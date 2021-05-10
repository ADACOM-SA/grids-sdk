package eu.grids.sdk.service.Impl;

import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.client.*;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import eu.grids.sdk.service.IGRIDSClientManager;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

public class GRIDSClientManager implements IGRIDSClientManager {

    private final URI clientsEndpoint;

    public GRIDSClientManager(URI clientsEndpoint){
        this.clientsEndpoint = clientsEndpoint;
    }

    public ClientInformation registerClient(ClientMetadata clientMetadata, String masterToken) throws IOException, ParseException {

        BearerAccessToken token = new BearerAccessToken(masterToken);

        clientMetadata.setGrantTypes(Collections.singleton(GrantType.AUTHORIZATION_CODE));

        ClientRegistrationRequest regRequest = new ClientRegistrationRequest(
                clientsEndpoint,
                clientMetadata,
                token
        );

        HTTPResponse httpResponse = regRequest.toHTTPRequest().send();

        ClientRegistrationResponse regResponse = ClientRegistrationResponse.parse(httpResponse);

        if (! regResponse.indicatesSuccess()) {
            // We have an error
            ClientRegistrationErrorResponse errorResponse = (ClientRegistrationErrorResponse)regResponse;
            System.err.println(errorResponse.getErrorObject());
            return null;
        }

        // Successful registration
        ClientInformationResponse successResponse = (ClientInformationResponse)regResponse;
        return successResponse.getClientInformation();

    }


    public URI getClientsEndpoint() {
        return clientsEndpoint;
    }
}
