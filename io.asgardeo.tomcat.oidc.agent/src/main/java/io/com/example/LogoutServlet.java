package io.com.example;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import io.asgardeo.java.oidc.sdk.HTTPSessionBasedOIDCProcessor;
import io.asgardeo.java.oidc.sdk.SSOAgentConstants;
import io.asgardeo.java.oidc.sdk.config.model.OIDCAgentConfig;
import io.asgardeo.java.oidc.sdk.exception.SSOAgentClientException;
import io.asgardeo.java.oidc.sdk.exception.SSOAgentException;
import io.asgardeo.java.oidc.sdk.exception.SSOAgentServerException;

@WebServlet("/unauthorize")
public class LogoutServlet extends HttpServlet {

    private HTTPSessionBasedOIDCProcessor oidcProcessor;
    private OIDCAgentConfig oidcAgentConfig;

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext servletContext = getServletContext();
        
        // Initialize OIDCAgentConfig from ServletContext
        if (servletContext.getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME) instanceof OIDCAgentConfig) {
            this.oidcAgentConfig = (OIDCAgentConfig) servletContext.getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
            System.out.println("oidcAgentConfig initialized: " + this.oidcAgentConfig);
        } else {
            throw new ServletException("Failed to initialize oidcAgentConfig. Check the ServletContext attribute name.");
        }
        
        // Initialize the OIDC Processor
        try {
            this.oidcProcessor = new HTTPSessionBasedOIDCProcessor(oidcAgentConfig);
        } catch (SSOAgentClientException e) {
            throw new ServletException("Failed to create OIDC Processor: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Perform logout operation
            oidcProcessor.logout(request, response);
        } catch (SSOAgentException e) {
            handleException(response, e);
        }
    }

    private void handleException(HttpServletResponse response, SSOAgentException e) throws IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Logout failed: " + e.getMessage());
    }
}
