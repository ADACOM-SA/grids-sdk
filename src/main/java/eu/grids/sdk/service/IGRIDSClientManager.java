package eu.grids.sdk.service;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.oauth2.sdk.client.ClientMetadata;

import java.io.IOException;
import java.net.URI;

public interface IGRIDSClientManager {

    ClientInformation registerClient(ClientMetadata clientMetadata, String masterToken) throws IOException, ParseException;

    ClientInformation getRegisteredClientInformation(URI registrationURI, String registrationAccessToken) throws IOException, ParseException;

    ClientInformation updateRegisteredClient(ClientInformation clientMetadata) throws IOException, ParseException;

    Boolean deleteRegisteredClient(URI registrationURI, String registrationAccessToken) throws IOException, ParseException;

}
