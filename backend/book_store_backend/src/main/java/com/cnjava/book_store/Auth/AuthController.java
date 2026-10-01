package com.cnjava.book_store.Auth;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserAccountRepository users;
  private final AuthService auth;

  public AuthController(UserAccountRepository users,AuthService auth){this.users=users;this.auth=auth;}

  public record RegisterRequest(String fullName,String email,String password){}
  public record LoginRequest(String email,String password){}

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest request,HttpServletRequest http){
    String name=request.fullName()==null?"":request.fullName().trim();
    String email=request.email()==null?"":request.email().trim().toLowerCase();
    String password=request.password()==null?"":request.password();

    if(name.length()<2) return ResponseEntity.badRequest().body(Map.of("message","Full name must contain at least 2 characters."));
    if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return ResponseEntity.badRequest().body(Map.of("message","Please enter a valid email."));
    if(password.length()<8) return ResponseEntity.badRequest().body(Map.of("message","Password must contain at least 8 characters."));
    if(users.existsByEmailIgnoreCase(email)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","An account with this email already exists."));

    UserAccount user=new UserAccount();
    user.setFullName(name);
    user.setEmail(email);
    user.setPasswordHash(PasswordUtil.hash(password));
    user=users.save(user);
    auth.login(http,user);
    return ResponseEntity.status(HttpStatus.CREATED).body(publicUser(user));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request,HttpServletRequest http){
    String email=request.email()==null?"":request.email().trim().toLowerCase();
    String password=request.password()==null?"":request.password();
    UserAccount user=users.findByEmailIgnoreCase(email).orElse(null);
    if(user==null || !PasswordUtil.matches(password,user.getPasswordHash()))
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Invalid email or password."));
    auth.login(http,user);
    return ResponseEntity.ok(publicUser(user));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest http){
    auth.logout(http);
    return ResponseEntity.ok(Map.of("success",true));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(HttpServletRequest http){
    return auth.currentUser(http)
      .<ResponseEntity<?>>map(user->ResponseEntity.ok(publicUser(user)))
      .orElseGet(()->ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("authenticated",false)));
  }

  private Map<String,Object> publicUser(UserAccount user){
    Map<String,Object> body=new LinkedHashMap<>();
    body.put("authenticated",true);
    body.put("id",user.getId());
    body.put("fullName",user.getFullName());
    body.put("email",user.getEmail());
    return body;
  }
}
