package com.cnjava.book_store.Promotion;

import java.util.*;
import com.cnjava.book_store.Auth.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
  private final AuthService auth;
  private final PromotionService promotions;

  public PromotionController(AuthService auth,PromotionService promotions){this.auth=auth;this.promotions=promotions;}

  @GetMapping("/my")
  public ResponseEntity<?> my(HttpServletRequest http){
    UserAccount user=auth.currentUser(http).orElse(null);
    if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Please sign in."));
    return ResponseEntity.ok(promotions.activeForUser(user.getId()));
  }
}
