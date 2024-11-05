package io.asgardeo.tomcat.oidc.agent;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenValidationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if token is present in request headers or parameters
        String token = httpRequest.getHeader("Authorization"); // Or use a different method to retrieve the token

        System.out.println("Parent token value is : " + token);

        if (token == null || token.isEmpty()) {
            // Set a custom header indicating redirection
            httpResponse.setHeader("X-Redirect-Parent", "true");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            return;
        }

        // Continue with the chain if token is valid
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
