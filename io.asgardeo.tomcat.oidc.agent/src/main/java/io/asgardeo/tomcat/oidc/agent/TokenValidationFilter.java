/*
 * Copyright (c) 2020, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
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
import java.util.Base64;
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
public class TokenValidationFilter implements Filter {

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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession();

        String requestURI = httpRequest.getRequestURI();
        String refererHeader = httpRequest.getHeader("Referer");
        String allowedReferer = "https://wso2sndev.service-now.com/";

        logger.debug("Referer is: " + refererHeader);

        if ((refererHeader != null && refererHeader.startsWith(allowedReferer))
                || session.getAttribute("hasauth") == true) {

            printRequestHeaders(httpRequest);
            session.setAttribute("hasauth", true);
            filterChain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect("/index.html");
        }

    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }

    private void printRequestHeaders(HttpServletRequest request) {
        // Get all header names and store them in a list
        java.util.List<String> headerNamesList = new java.util.ArrayList<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();

        // Collect all the header names
        while (headerNames.hasMoreElements()) {
            headerNamesList.add(headerNames.nextElement());
        }

        // Use a for loop to log each header name and its value
        for (String headerName : headerNamesList) {
            String headerValue = request.getHeader(headerName);
            logger.info("Header: " + headerName + " = " + headerValue);
        }
    }

}