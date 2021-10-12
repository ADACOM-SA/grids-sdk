package eu.grids.sdk;

import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.oauth2.sdk.client.ClientMetadata;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import eu.grids.sdk.service.Impl.GRIDSClientManager;
import eu.grids.sdk.service.Impl.GRIDSIssuer;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GRIDSClientManagerTest {


    private String masterToken = "eyJhbGciOiJIUzI1NiIs....-2_Fm9oJ2x_pJlo20";


    URI uri;
    String token;

    @Test
    public void testACreateClient() throws IOException, URISyntaxException, ParseException {

        GRIDSIssuer issuer = new GRIDSIssuer(new URI("https://vm.project-grids.eu:8180/auth/realms/grids/"));

        OIDCProviderMetadata metadata = issuer.getOPMetadata();

        GRIDSClientManager manager = new GRIDSClientManager(metadata.getRegistrationEndpointURI());

        OIDCClientMetadata clientMetadata = new OIDCClientMetadata();
        clientMetadata.setName("Adacom test");
        clientMetadata.setGrantTypes(Collections.singleton(GrantType.AUTHORIZATION_CODE));
        clientMetadata.setRedirectionURI(URI.create("https://dc-test.adacom/callback"));
        clientMetadata.setJWKSetURI(new URI("https://dc-test.adacom/jwks"));

        ClientInformation clientInformation = manager.registerClient(clientMetadata, masterToken);

        System.out.println(clientInformation.getID());
        System.out.println(clientInformation.getIDIssueDate());
        System.out.println(clientInformation.getMetadata());
        System.out.println(clientInformation.getRegistrationURI());
        System.out.println(clientInformation.getRegistrationAccessToken());
        System.out.println(clientInformation.getSecret());
        System.out.println(clientInformation.getMetadata().toJSONObject());

        uri = clientInformation.getRegistrationURI();
        token = clientInformation.getRegistrationAccessToken().toString();

        testBGetClient();

        testDeleteClient();

    }

    @Test
    public void testCUpdateClient() throws IOException, URISyntaxException, ParseException {

        GRIDSIssuer issuer = new GRIDSIssuer(new URI("https://vm.project-grids.eu:8180/auth/realms/grids/"));

        OIDCProviderMetadata metadata = issuer.getOPMetadata();

        GRIDSClientManager manager = new GRIDSClientManager(metadata.getRegistrationEndpointURI());

        OIDCClientMetadata clientMetadata = new OIDCClientMetadata();
        clientMetadata.setName("Adacom test");
        clientMetadata.setGrantTypes(Collections.singleton(GrantType.AUTHORIZATION_CODE));
        clientMetadata.setRedirectionURI(URI.create("https://dc-test.adacom/callback"));
        clientMetadata.setJWKSetURI(new URI("https://dc-test.adacom/jwks"));

        ClientInformation clientInformation = manager.registerClient(clientMetadata, masterToken);


        System.out.println(clientInformation.getID());
        System.out.println(clientInformation.getIDIssueDate());
        System.out.println(clientInformation.getMetadata());
        System.out.println(clientInformation.getRegistrationURI());
        System.out.println(clientInformation.getRegistrationAccessToken());
        System.out.println(clientInformation.getSecret());
        System.out.println(clientInformation.getMetadata().toJSONObject());

        clientInformation.getMetadata().setName("Adacom test 3");

        ClientInformation clientInformationUpdated = manager.updateRegisteredClient(clientInformation);

        System.out.println(clientInformationUpdated.getID());
        System.out.println(clientInformationUpdated.getIDIssueDate());
        System.out.println(clientInformationUpdated.getMetadata());
        System.out.println(clientInformationUpdated.getRegistrationURI());
        System.out.println(clientInformationUpdated.getRegistrationAccessToken());
        System.out.println(clientInformationUpdated.getSecret());
        System.out.println(clientInformationUpdated.getMetadata().toJSONObject());

        uri = clientInformation.getRegistrationURI();
        token = clientInformationUpdated.getRegistrationAccessToken().toString();

        testDeleteClient();

    }

    @Test
    public void testGetCustomInfo() throws IOException, URISyntaxException, ParseException {

        uri = new URI("https://vm.project-grids.eu:8180/auth/realms/grids/clients-registrations/openid-connect/6293f853-961b-4c84-97d6-26151714acbd");
        token = "eyJhbGciOiJIUzI1....2ofrg7I89g6UGAo";
        testBGetClient();

    }

    private void testBGetClient() throws IOException, URISyntaxException, ParseException {

        GRIDSIssuer issuer = new GRIDSIssuer(new URI("https://vm.project-grids.eu:8180/auth/realms/grids/"));

        OIDCProviderMetadata metadata = issuer.getOPMetadata();

        GRIDSClientManager manager = new GRIDSClientManager(metadata.getRegistrationEndpointURI());

        //uri = new URI("https://vm.project-grids.eu:8180/auth/realms/grids/clients-registrations/openid-connect/2ac37c12-c827-4d48-9d19-d3cc5bda11f1");
        //token = "eyJhbGciOiJ....2sYDnkMwTDBcJyE84g";
        ClientInformation clientInformation = manager.getRegisteredClientInformation(uri, token);


        System.out.println(clientInformation.getID());
        System.out.println(clientInformation.getIDIssueDate());
        System.out.println(clientInformation.getMetadata());
        System.out.println(clientInformation.getRegistrationURI());
        System.out.println(clientInformation.getRegistrationAccessToken());
        System.out.println(clientInformation.getSecret());
        System.out.println(clientInformation.getMetadata().toJSONObject());

    }

    public void testDeleteClient() throws IOException, URISyntaxException, ParseException {

        GRIDSIssuer issuer = new GRIDSIssuer(new URI("https://vm.project-grids.eu:8180/auth/realms/grids/"));

        OIDCProviderMetadata metadata = issuer.getOPMetadata();

        GRIDSClientManager manager = new GRIDSClientManager(metadata.getRegistrationEndpointURI());

        //uri = new URI("https://vm.project-grids.eu:8180/auth/realms/grids/clients-registrations/openid-connect/7424743f-6551-4fe7-9597-6e04c46235fa");
        //token = "eyJhbGciOiJIUzI1NiIs.....RrpZSTebQTc9yO01wlE0";

        Boolean result = manager.deleteRegisteredClient(uri, token);

        Assert.assertTrue(result);

    }


}
