package videverse.vv_editor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private static final String API_TOKEN = "your-static-api-token"; // Replace with your actual token

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // Explicitly disable CSRF
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/videos/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/videos/upload").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/videos/trim").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/videos/merge").authenticated()
            .anyRequest().permitAll()
        )
        .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public TokenAuthenticationFilter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter(API_TOKEN);
  }
}