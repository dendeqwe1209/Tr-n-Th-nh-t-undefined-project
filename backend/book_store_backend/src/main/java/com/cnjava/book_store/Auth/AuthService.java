package com.cnjava.book_store.Auth;

import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  public static final String SESSION_USER_ID="USER_ID";
  private final UserAccountRepository users;

  public AuthService(UserAccountRepository users){this.users=users;}

  public Optional<UserAccount> currentUser(HttpServletRequest request){
    HttpSession session=request.getSession(false);
    if(session==null) return Optional.empty();
    Object value=session.getAttribute(SESSION_USER_ID);
    if(!(value instanceof Number)) return Optional.empty();
    return users.findById(((Number)value).longValue());
  }

  public void login(HttpServletRequest request,UserAccount user){
    HttpSession old=request.getSession(false);
    if(old!=null) old.invalidate();
    HttpSession session=request.getSession(true);
    session.setAttribute(SESSION_USER_ID,user.getId());
    session.setMaxInactiveInterval(60*60*24*7);
  }

  public void logout(HttpServletRequest request){
    HttpSession session=request.getSession(false);
    if(session!=null) session.invalidate();
  }
}
