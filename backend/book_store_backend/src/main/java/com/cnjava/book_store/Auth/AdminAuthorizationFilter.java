package com.cnjava.book_store.Auth;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class AdminAuthorizationFilter extends OncePerRequestFilter {
  private final AuthService auth;
  public AdminAuthorizationFilter(AuthService auth){this.auth=auth;}

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request){
    String uri=request.getRequestURI();
    if("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
    return !(uri.startsWith("/api/admin") || uri.startsWith("/admin/staff-management"));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException{
    UserAccount user=auth.currentUser(request).orElse(null);
    if(user==null){
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Admin login required.\"}");
      return;
    }
    if(user.getRole()!=UserRole.ADMIN){
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Administrator permission required.\"}");
      return;
    }
    chain.doFilter(request,response);
  }
}
