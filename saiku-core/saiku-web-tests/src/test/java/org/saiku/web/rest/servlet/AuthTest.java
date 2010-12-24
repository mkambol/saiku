package org.saiku.web.rest.servlet;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream; 
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.security.oauth.common.signature.HMAC_SHA1SignatureMethod;
import org.springframework.security.oauth.common.signature.SharedConsumerSecret;
import org.springframework.security.oauth.consumer.BaseProtectedResourceDetails;
import org.springframework.security.oauth.consumer.CoreOAuthConsumerSupport;
import org.springframework.security.oauth.consumer.InMemoryProtectedResourceDetailsService;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.net.DefaultOAuthURLStreamHandlerFactory;
import org.springframework.security.oauth.consumer.token.OAuthConsumerToken;
import org.springframework.security.oauth2.common.DefaultOAuth2SerializationService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class AuthTest extends AbstractServiceTest{
//
// 
//
//    @Test
//    public void testGetRequestToken() throws Exception {
//        CoreOAuthConsumerSupport support = new CoreOAuthConsumerSupport();
//        support.setStreamHandlerFactory(new DefaultOAuthURLStreamHandlerFactory());
//        InMemoryProtectedResourceDetailsService service = new InMemoryProtectedResourceDetailsService();
//        HashMap<String, ProtectedResourceDetails> detailsStore = new HashMap<String, ProtectedResourceDetails>();
//        BaseProtectedResourceDetails googleDetails = new BaseProtectedResourceDetails();
//        googleDetails.setRequestTokenURL("http://localhost:9999/saiku-web-2.0-SNAPSHOT/oauth/request_token");
//        googleDetails.setAccessTokenURL("http://localhost:9999/saiku-web-2.0-SNAPSHOT/oauth/access_token");
//        googleDetails.setUserAuthorizationURL("http://localhost:9999/saiku-web-2.0-SNAPSHOT/oauth/authorize");
//        googleDetails.setConsumerKey("tonr-consumer-key");
//        googleDetails.setSharedSecret(new SharedConsumerSecret("SHHHHH"));
//        googleDetails.setId("google");
//        googleDetails.setUse10a(true);
//        googleDetails.setSignatureMethod(HMAC_SHA1SignatureMethod.SIGNATURE_NAME);
//        googleDetails.setRequestTokenHttpMethod("GET");
//        /*HashMap<String, String> additional = new HashMap<String, String>();
//        additional.put("scope", "http://picasaweb.google.com/data");
//        googleDetails.setAdditionalParameters(additional);*/
//        detailsStore.put(googleDetails.getId(), googleDetails);
//        service.setResourceDetailsStore(detailsStore);
//        support.setProtectedResourceDetailsService(service);
//        OAuthConsumerToken token = support.getUnauthorizedRequestToken("google", "urn:mycallback");
//        
//        authorizeRequestToken(token);
//        OAuthConsumerToken accessToken = support.getAccessToken( token, requestTokenVerifier );
//        getProtectedResource(accessToken, support);
//      System.out.println(token.getValue());
//      System.out.println(token.getSecret());
//    }
//    static String requestTokenVerifier;
//    public static void authorizeRequestToken(OAuthConsumerToken requestToken) throws HttpException, IOException {
//                int resultCode = 0;
//                HttpClient httpClient = new HttpClient();
//                PostMethod authorizeMethod = new PostMethod( "http://localhost:9999/saiku-web-2.0-SNAPSHOT/oauth/authorize" );
//                authorizeMethod.addParameter( "requestToken", requestToken.getValue() );
//                authorizeMethod.addParameter( "authorize", "Authorize" );
//                resultCode = httpClient.executeMethod( authorizeMethod );
//                String body = authorizeMethod.getResponseBodyAsString();
//                String redirectURL = authorizeMethod.getResponseHeader( "Location" ).getValue();
//                if ( redirectURL != null && redirectURL.indexOf( "oauth_verifier" ) > -1 ) {
//                    requestTokenVerifier = redirectURL.substring( redirectURL.indexOf( "oauth_verifier" ) + 15 );
//                }
//
//            }
//
//    public static void getProtectedResource(OAuthConsumerToken accessToken, CoreOAuthConsumerSupport consumerSupport) throws IOException {
//        InputStream is = consumerSupport.readProtectedResource( new URL( "http://localhost:9999/saiku-web-2.0-SNAPSHOT/rest/saiku/admin/datasources" ), accessToken, "GET" );
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        IOUtils.copy( is, baos );
//        is.close();
//        baos.close();
//        String string =  new String( Arrays.copyOf( baos.toByteArray(), 30 ) );
//        
//        System.out.print(string);
//    }
//
    @Test
    public void testHappyDay() throws Exception {
        int port = 9999;
        Client client = Client.create();
        client.setFollowRedirects(false);

        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("grant_type", "password");
        formData.add("client_id", "my-trusted-client");
        formData.add("username", "marissa");
        formData.add("password", "koala");
        WebResource webResource = client.resource("http://localhost:9999/");
        ClientResponse response = webResource.path("/saiku/oauth/authorize")
          .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
          .post(ClientResponse.class, formData);
        assertEquals(200, response.getClientResponseStatus().getStatusCode());
        assertEquals("no-store", response.getHeaders().getFirst("Cache-Control"));

        DefaultOAuth2SerializationService serializationService = new DefaultOAuth2SerializationService();
        OAuth2AccessToken accessToken = serializationService.deserializeJsonAccessToken(response.getEntityInputStream());

        //now try and use the token to access a protected resource.

        //first make sure the resource is actually protected.
        response = client.resource("http://localhost:" + port + "/saiku/docs/index.html").get(ClientResponse.class);
        assertFalse(200 == response.getClientResponseStatus().getStatusCode());

        //now make sure an authorized request is valid.
        response = client.resource("http://localhost:" + port + "/saiku/docs/index.html")
          .header("Authorization", String.format("OAuth %s", accessToken.getValue()))
          .get(ClientResponse.class);
        assertEquals(200, response.getClientResponseStatus().getStatusCode());
      }

}
