/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.asgardeo.tomcat.oidc.agent;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import io.asgardeo.java.oidc.sdk.HTTPSessionBasedOIDCProcessor;
import io.asgardeo.java.oidc.sdk.SSOAgentConstants;
import io.asgardeo.java.oidc.sdk.bean.RequestContext;
import io.asgardeo.java.oidc.sdk.bean.SessionContext;
import io.asgardeo.java.oidc.sdk.bean.User;
import io.asgardeo.java.oidc.sdk.config.model.OIDCAgentConfig;
import io.asgardeo.java.oidc.sdk.exception.SSOAgentClientException;
import io.asgardeo.java.oidc.sdk.exception.SSOAgentException;
import io.asgardeo.java.oidc.sdk.exception.SSOAgentServerException;
import io.asgardeo.java.oidc.sdk.request.OIDCRequestResolver;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import okhttp3.*;

/**
 * OIDCAgentFilter is the Filter class responsible for building
 * requests and handling responses for authentication, SLO and session
 * management for the OpenID Connect flows, using the io-asgardeo-oidc-sdk.
 * It is an implementation of the base class, {@link Filter}.
 * OIDCAgentFilter verifies if:
 * <ul>
 * <li>The request is a URL to skip
 * <li>The request is a Logout request
 * <li>The request is already authenticated
 * </ul>
 * <p>
 * and build and send the request, handle the response,
 * or forward the request accordingly.
 */
public class OIDCAgentFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(OIDCAgentFilter.class);

    protected FilterConfig filterConfig = null;
    OIDCAgentConfig oidcAgentConfig;
    HTTPSessionBasedOIDCProcessor oidcManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        this.filterConfig = filterConfig;
        ServletContext servletContext = filterConfig.getServletContext();
        if (servletContext.getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME) instanceof OIDCAgentConfig) {
            this.oidcAgentConfig = (OIDCAgentConfig) servletContext.getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
            System.out.println("oidcAgentConfig initialized: " + this.oidcAgentConfig);
        } else {
            System.err.println("Failed to initialize oidcAgentConfig. Check the ServletContext attribute name.");
        }
        try {
            this.oidcManager = new HTTPSessionBasedOIDCProcessor(oidcAgentConfig);
        } catch (SSOAgentClientException e) {
            throw new SSOAgentException(e.getMessage(), e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();

        OIDCRequestResolver requestResolver = new OIDCRequestResolver(request, oidcAgentConfig);

        if (requestResolver.isSkipURI()) {
            filterChain.doFilter(servletRequest, servletResponse);
            System.out.println("Request URI: " + requestURI);
            System.out.println("Skip URIs: " + oidcAgentConfig.getSkipURIs());
            return;
        }

        if (requestResolver.isLogoutURL()) {
            try {
                oidcManager.logout(request, response);
            } catch (SSOAgentException e) {
                handleException(request, response, e);
            }
            return;
        }

        if (requestResolver.isCallbackResponse()) {
            RequestContext requestContext = getRequestContext(request);
            if (requestContext == null) {
                handleException(request, response, new SSOAgentServerException("Request context is null."));
                return;
            }

            try {
                oidcManager.handleOIDCCallback(request, response);
            } catch (SSOAgentException e) {
                handleException(request, response, e);
                return;
            }
            // Check for logout scenario.
            if (requestResolver.isLogout()) {
                response.sendRedirect(oidcAgentConfig.getIndexPage());
                return;
            }

            String homePage = resolveTargetPage(request, requestContext);
            if (logger.isDebugEnabled()) {
                logger.debug("Redirection home page is set to " + homePage);
            }
            if (StringUtils.isBlank(homePage)) {
                handleException(request, response, new SSOAgentClientException("Redirection target is null."));
                return;
            }
            // System.out.println("Hit the filter once");
            // if (hasValidSubscriptionOrAccess(request)){
            // response.sendRedirect(homePage);
            // }
            // logAndDenyAccess(response);

            response.sendRedirect(homePage);
            // returnToken(request);
            return;
        }

        if (!isActiveSessionPresent(request)) {
            try {
                oidcManager.sendForLogin(request, response);

            } catch (SSOAgentException e) {
                handleException(request, response, e);
            }
        } else {
            if (!hasValidSubscriptionOrAccess(request)) {
                logAndDenyAccess(request, response);
            }
            filterChain.doFilter(request, response);

            // String mytoken = returnToken(request);

            // // Check if the token is expired
            // if (isTokenExpired(mytoken)) {
            // // Log and inform the user about the session expiration
            // System.out.println("Your session is expired. Please renew your session.");

            // // Redirect to a session renewal endpoint or show a renewal UI
            // response.sendRedirect("/renewSession.html"); // Redirecting to a renewal page
            // return;
            // } else {
            // filterChain.doFilter(request, response); // Proceed normally if token is
            // valid
            // }
        }
    }

    private String resolveTargetPage(HttpServletRequest request, RequestContext requestContext) {

        if (StringUtils.isNotBlank(oidcAgentConfig.getHomePage())) {
            return oidcAgentConfig.getHomePage();
        }
        if (requestContext != null && StringUtils.isNotBlank((CharSequence) requestContext.getParameter(
                SSOAgentConstants.REDIRECT_URI_KEY))) {
            return requestContext.getParameter(SSOAgentConstants.REDIRECT_URI_KEY).toString();
        }
        if (StringUtils.isNotBlank(oidcAgentConfig.getIndexPage())) {
            return oidcAgentConfig.getIndexPage();
        }

        // If all the checks fail, set root path as the target page.
        String requestUrl = request.getRequestURL().toString();
        return requestUrl.substring(0, requestUrl.length() - request.getServletPath().length());
    }

    private RequestContext getRequestContext(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute(SSOAgentConstants.REQUEST_CONTEXT) != null) {
            return (RequestContext) request.getSession(false).getAttribute(SSOAgentConstants.REQUEST_CONTEXT);
        }
        return null;
    }

    @Override
    public void destroy() {

    }

    boolean isActiveSessionPresent(HttpServletRequest request) {

        HttpSession currentSession = request.getSession(false);

        return currentSession != null
                && currentSession.getAttribute(SSOAgentConstants.SESSION_CONTEXT) != null
                && currentSession.getAttribute(SSOAgentConstants.SESSION_CONTEXT) instanceof SessionContext;
    }

    void clearSession(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    protected void handleException(HttpServletRequest request, HttpServletResponse response, SSOAgentException e)
            throws ServletException, IOException {

        String errorPage = oidcAgentConfig.getErrorPage();
        if (StringUtils.isBlank(errorPage)) {
            errorPage = buildErrorPageURL(oidcAgentConfig, request);
        }
        if (errorPage.trim().charAt(0) != '/') {
            errorPage = "/" + errorPage;
        }
        clearSession(request);
        logger.log(Level.FATAL, e.getMessage());
        request.setAttribute(SSOAgentConstants.AGENT_EXCEPTION, e);
        RequestDispatcher requestDispatcher = request.getServletContext().getRequestDispatcher(errorPage);
        requestDispatcher.forward(request, response);
    }

    private String buildErrorPageURL(OIDCAgentConfig oidcAgentConfig, HttpServletRequest request) {

        if (StringUtils.isNotBlank(oidcAgentConfig.getErrorPage())) {
            return oidcAgentConfig.getErrorPage();
        } else if (StringUtils.isNotBlank(oidcAgentConfig.getIndexPage())) {
            return oidcAgentConfig.getIndexPage();
        }
        return SSOAgentConstants.DEFAULT_CONTEXT_ROOT;
    }

    private boolean hasValidSubscriptionOrAccess(HttpServletRequest request) {
        String email = getUserEmail(request);
        // String idToken = getIdToken(request);
        String idToken = "eyJ4NXQiOiJ4ek1kUHdzZ2RXZHIwSjBPaW1sQjltNnZfcEUiLCJraWQiOiJaamMxWlRWbVlUaGhNakpsTVRZMU5EZ3dObU13WW1GbE1HWTBObVF5TURjME5tWmpOV1ZtWkRCbE5qZzRNemN5TTJObVpUSmtORGs1TjJJNE5UZzVPUV9SUzI1NiIsInR5cCI6ImF0K2p3dCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI3Mzc1MmI5ZS1hMTcyLTQ0NjItYTg4Zi0zYWY3YzhmOWEzY2MiLCJhdXQiOiJBUFBMSUNBVElPTiIsImF1ZCI6WyJOZ2ZBdVJqamRkT1JkQV8zTEd3U3FmRlhmOFVhIiwiY2hvcmVvOmRlcGxveW1lbnQ6c2FuZGJveCJdLCJuYmYiOjE3Mjk0OTk4MTUsImF6cCI6Ik5nZkF1UmpqZGRPUmRBXzNMR3dTcWZGWGY4VWEiLCJvcmdfaWQiOiIxMTczNzQyMi03MDc4LTg1MTAtMTA3NC04MjA4OTI2MzExOTQiLCJpc3MiOiJodHRwczpcL1wvYXBpLmFzZ2FyZGVvLmlvXC90XC93c28yXC9vYXV0aDJcL3Rva2VuIiwiZXhwIjoxNzI5NTAwNzE1LCJvcmdfbmFtZSI6IndzbzIiLCJpYXQiOjE3Mjk0OTk4MTUsImp0aSI6ImM3NzNmNDE1LWU4ZWEtNGRlOS04YjFlLWRiOGYyMzNkZDM0NSIsImNsaWVudF9pZCI6Ik5nZkF1UmpqZGRPUmRBXzNMR3dTcWZGWGY4VWEifQ.QsHQYphxt-e-K2wRhWaXcnTiHVcHRKJzUNru_-Bwcr-Kbg3kCWycvk2CqiEh2pROMPajAdMk5f0ZFeFbrrwu6Ki34uMMYcMKtgduWH5A1AVX9CzbMi8mkexTCVKKJbf2PAPRXNktXJICWR-CtRNiSudXRmId7VOtV1tORyNvNv9rydh42GKC5kswnXlj-VV-fygQAUQGpSOdxUiM8grUgUVhnO0OCr4mmWNH05E6AFmnVbviV7xs7fIvLw_8hTSIOutZ1EEWbGNKXQfcFi9chepWFqUZIomgZnzsICFvC2Xj27g2so6HoYl74mpJ8zFqnKFBUMeXLFnj_fgxX_eCrQ";
        String mytoken = returnToken(request);
        if (idToken == null || isTokenExpired(mytoken)) {
            logger.warn("Invalid or expired ID token for user: " + email);
            return false;
        }

        if (checkOrgEmail(request) || checkSubscription(email, idToken)) {
            return true;
        }
        return false;
        // return checkSubscription(email, idToken);

    }

    private boolean isTokenExpired(String idToken) {
        try {
            SignedJWT signedJWTIdToken = SignedJWT.parse(idToken);
            JWTClaimsSet claimsSet = signedJWTIdToken.getJWTClaimsSet();
            Date expirationTime = claimsSet.getExpirationTime();
            // System.out.println("Expire token time is : "+ expirationTime);
            return expirationTime.before(new Date()); // Check if the token is expired
        } catch (ParseException e) {
            logger.error("Failed to parse ID token: ", e);
            return true; // Assume expired if we cannot parse
        }
    }

    private void logAndDenyAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        logger.warn("Unauthorized access attempt: User lacks subscription or valid organization email.");
        System.out.println("Unauthorized User");
        response.sendRedirect("/unauthorized");
    }

    private String getUserEmail(HttpServletRequest request) {

        final HttpSession currentSession = request.getSession(false);
        final SessionContext sessionContext = (SessionContext) currentSession
                .getAttribute(SSOAgentConstants.SESSION_CONTEXT);
        final String idToken = sessionContext.getIdToken();

        String scopes = "";

        ServletContext servletContext = filterConfig.getServletContext();
        if (servletContext.getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME) != null) {
            OIDCAgentConfig oidcAgentConfig = (OIDCAgentConfig) servletContext
                    .getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
            scopes = oidcAgentConfig.getScope().toString();
        }

        try {

            SignedJWT signedJWTIdToken = SignedJWT.parse(idToken);

            JWTClaimsSet claimsSet = signedJWTIdToken.getJWTClaimsSet();

            // String email = claimsSet.getStringClaim("email");
            // String email = "testsubuser@sntest.com";
            String email = "shayan@wso2.com";
            return email;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean checkSubscription(String email, String token) {
        Properties properties = new Properties();

        // Load the properties file
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("oidc-sample-app.properties")) {
            if (input == null) {
                System.err.println("Unable to find oidc-sample-app.properties");
                return false;
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Get the subscription API endpoint from the properties file
        String apiEndpoint = properties.getProperty("subscriptionCall");
        if (apiEndpoint == null) {
            System.err.println("API endpoint not defined in properties file.");
            return false;
        }

        OkHttpClient client = new OkHttpClient.Builder().build();
        String apiUrl = apiEndpoint + "?customerEmail=" + email;

        // Create an empty request body
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");

        // Build the request
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Execute the request and handle the response
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                return jsonResponse.contains("\"isValidCustomer\":true");
            } else {
                System.err.println("Request failed with status code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String returnToken(HttpServletRequest request) {

        final HttpSession currentSession = request.getSession(false);
        final SessionContext sessionContext = (SessionContext) currentSession
                .getAttribute(SSOAgentConstants.SESSION_CONTEXT);

        final String idToken = sessionContext.getIdToken();

        // System.out.println("this is intro ID token " + idToken);
        return idToken;
    }

    private boolean checkOrgEmail(HttpServletRequest request) {

        String email = getUserEmail(request);
        String domain = "@wso2.com";

        if (email.trim().endsWith(domain)) {
            // System.out.println("User is Wso2 Employee: " + email);
            return true;
        }
        System.out.println("User is not a Wso2 Employee: " + email);
        return false;
    }

    public String sendRequestWithJwt(String url, String jwtToken) throws IOException {
        OkHttpClient client = new OkHttpClient();
    
        // Build the request with the Authorization header
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + jwtToken) // Add JWT token
            .build();
    
        // Execute the request and get the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
    
            // Return the response body as a string
            return response.body().string();
        }
    }

}
