package eu.grids.sdk.service;

import com.nimbusds.openid.connect.sdk.OIDCClaimsRequest;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

public interface IGRIDSIssuer {

    OIDCProviderMetadata getOPMetadata();

    String getAuthorizationUrl(OIDCClaimsRequest claims);

    OIDCTokens requestToken(String callbackResponse);

    UserInfo getUserInfo(String token);

    UserInfo getDPUserInfo(URI userInfoURI, String token) throws ParseException;

}
