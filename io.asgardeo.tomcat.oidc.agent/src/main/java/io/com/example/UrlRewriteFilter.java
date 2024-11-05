package io.com.example;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class UrlRewriteFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        // Check if the URL matches "home" or "access-denied"
        if (uri.equals("/home") || uri.equals("/unauthorized")) {
            // Forward to the corresponding .html file
            String newUri = uri + ".html";
            // System.out.println("New URl : " + newUri);
            request.getRequestDispatcher(newUri).forward(request, response);
            
        } else {
            // Continue with the normal request if no rewriting is needed
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Cleanup logic, if needed
    }
}
