package eu.grids.sdk.service;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.oauth2.sdk.client.ClientMetadata;

import java.io.IOException;

public interface IGRIDSClientManager {

    ClientInformation registerClient(ClientMetadata clientMetadata, String masterToken) throws IOException, ParseException;

}
