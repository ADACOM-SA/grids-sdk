package eu.grids.sdk;


import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.util.JSONArrayUtils;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.OIDCClaimsRequest;
import com.nimbusds.openid.connect.sdk.assurance.claims.VerifiedClaimsSetRequest;
import com.nimbusds.openid.connect.sdk.assurance.evidences.IdentityEvidence;
import com.nimbusds.openid.connect.sdk.assurance.evidences.IdentityEvidenceType;
import com.nimbusds.openid.connect.sdk.claims.DistributedClaims;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import eu.grids.sdk.service.Impl.GRIDSIssuer;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class GRIDSIssuerTest {


    @Test
    public void testGetAuthorizationUrl() throws URISyntaxException, ParseException {

        GRIDSIssuer issuer = new GRIDSIssuer(
                new URI("https://vm.project-grids.eu:8180/auth/realms/grids/"),
                "6293f853-.....-26151714acbd",
                "e4fae62f-.....-d405d6311d1c",
                new URI("https://dc-test.adacom/callback"),
                null
        );

        OIDCProviderMetadata metadata = issuer.getOPMetadata();

        JSONObject verification = new JSONObject();
        verification.put("trust_framework", "grids_kyb");
        verification.put("userinfo_endpoint", "https://dp.kompany.com:8050/userinfo");

        JSONObject idVerification = new JSONObject();
        idVerification.put("trust_framework", "eidas");

        OIDCClaimsRequest claims = new OIDCClaimsRequest()
                .withIDTokenVerifiedClaimsRequest(
                        new VerifiedClaimsSetRequest()
                                .withVerificationJSONObject(idVerification)
                                .add("family_name")
                                .add("given_name")
                                .add("birthdate")
                                .add("person_identifier")
                                .add("place_of_birth")
                                .add("address")
                                .add("gender")
                )
                .withUserInfoVerifiedClaimsRequest(
                        new VerifiedClaimsSetRequest()
                                .withVerificationJSONObject(verification)
                                .add("family_name")
                                .add("given_name")
                                .add("birthdate")
                                .add("legal_name")
                                .add("legal_person_identifier")
                                .add("lei")
                                .add("vat_registration")
                                .add("address")
                                .add("tax_reference")
                                .add("sic")
                                .add("business_role")
                                .add("sub_jurisdiction")
                                .add("trading_status")
                );

        System.out.println(claims.toJSONString());

        String url = issuer.getAuthorizationUrl(claims);

        System.out.println(url);
    }

    @Test
    public void testRequestToken() throws URISyntaxException {

        GRIDSIssuer issuer = new GRIDSIssuer(
                new URI("https://vm.project-grids.eu:8180/auth/realms/grids/"),
                "6293f853-......-26151714acbd",
                "e4fae62f-......-d405d6311d1c",
                new URI("https://dc-test.adacom/callback"),
                null
        );

        OIDCProviderMetadata metadata = issuer.getOPMetadata();

        OIDCClaimsRequest claims = new OIDCClaimsRequest();

        OIDCTokens tokens = issuer.requestToken("https://dc-test.adacom/callback?state=SKJtF_-1rkf2kTteyleJh78TP_lPLNOQRen-WNLO0Eg&session_state=bda456ff-4c00-4a7d-9ca9-dcbf7814c51a&code=9a425927-bb91-4d7b-9d59-f113df04772b.bda456ff-4c00-4a7d-9ca9-dcbf7814c51a.6293f853-961b-4c84-97d6-26151714acbd");

        System.out.println(tokens.toJSONObject());
        System.out.println(tokens.getAccessToken().getValue());

        UserInfo userInfo = issuer.getUserInfo(tokens.getAccessToken().getValue());

        System.out.println(userInfo.toJSONObject());

        List<String> claimNamesToGet = Arrays.asList("given_name", "family_name", "birthdate");
        Set<DistributedClaims> set = userInfo.getDistributedClaims();
        for (String claimName : claimNamesToGet) {
            for (DistributedClaims c : set) {
                Set<String> claimNames = c.getNames();

                if (claimNames.contains(claimName)) {
                    URI dpEndpoing = c.getSourceEndpoint();
                    String token = c.getAccessToken().getValue();
//                    UserInfo userInfo = issuer.getDPUserInfo(token);
//
//                    VerifiedClaimsSet verifiedClaims = userInfo.getVerifiedClaims().get(0);
//                    PersonClaims verifiedPersonClaims = verifiedClaims.getClaimsSet();
//                    String givenName = verifiedPersonClaims.getGivenName(); // Max
//                    String familynName = verifiedPersonClaims.getFamilyName(); // Meier
//                    String birthDay = verifiedPersonClaims.getBirthdate(); // 1956-01-28
                }
            }
        }

    }


    @Test
    public void testUserInfo() throws URISyntaxException {

        GRIDSIssuer issuer = new GRIDSIssuer(
                new URI("https://vm.project-grids.eu:8180/auth/realms/grids/"),
                "6293f853-...-26151714acbd",
                "e4fae62f-.....-d405d6311d1c",
                new URI("https://dc-test.adacom/callback"),
                null
        );

        String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI......P0f0Z7DOnerQTRp90ecYQ";

        UserInfo userInfo = issuer.getUserInfo(accessToken);

        System.out.println(userInfo.toJSONObject());


        Set<DistributedClaims> set = userInfo.getDistributedClaims();
        for (DistributedClaims claim : set) {

        }

    }

}
