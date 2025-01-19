package videverse.vv_editor.config;



import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")  // Apply this filter to all incoming requests
public class TokenAuthenticationFilter implements Filter {

  private static final String EXPECTED_TOKEN = "your-static-api-token";  // Replace with your actual token

  public TokenAuthenticationFilter(String apiToken) {
    if(apiToken == null || apiToken.isEmpty()) {
      throw new IllegalArgumentException("API token must not be null or empty");
    }
    if(!apiToken.equals(EXPECTED_TOKEN)) {
      throw new IllegalArgumentException("Invalid API token");
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Get the Authorization header
    String token = httpRequest.getHeader("Authorization");

    if (token == null || !token.startsWith("Bearer ") || !token.substring(7).equals(EXPECTED_TOKEN)) {
      // If no token is present or token is invalid, return an unauthorized error
      httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid or missing token");
      return;
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}