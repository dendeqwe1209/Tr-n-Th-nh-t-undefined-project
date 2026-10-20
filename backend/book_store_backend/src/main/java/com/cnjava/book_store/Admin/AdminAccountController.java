package com.cnjava.book_store.Admin;

import java.util.*;
import com.cnjava.book_store.Auth.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminAccountController {
  private final UserAccountRepository users;
  private final AuthService auth;

  public AdminAccountController(UserAccountRepository users,AuthService auth){this.users=users;this.auth=auth;}

  public record RoleRequest(String role){}

  @GetMapping
  public List<Map<String,Object>> list(){
    return users.findAll().stream().sorted(Comparator.comparing(UserAccount::getCreatedAt)).map(u->{
      Map<String,Object> m=new LinkedHashMap<>();
      m.put("id",u.getId());m.put("fullName",u.getFullName());m.put("email",u.getEmail());
      m.put("username",u.getUsername());m.put("role",u.getRole().name());m.put("createdAt",u.getCreatedAt());
      return m;
    }).toList();
  }

  @PutMapping("/{id}/role")
  public ResponseEntity<?> role(@PathVariable long id,@RequestBody RoleRequest request,HttpServletRequest http){
    UserAccount target=users.findById(id).orElse(null);
    if(target==null) return ResponseEntity.notFound().build();

    UserRole role;
    try{role=UserRole.valueOf(request.role()==null?"":request.role().trim().toUpperCase());}
    catch(Exception e){return ResponseEntity.badRequest().body(Map.of("message","Role must be USER or ADMIN."));}

    UserAccount current=auth.currentUser(http).orElse(null);
    if(current!=null&&current.getId()==target.getId()&&role!=UserRole.ADMIN)
      return ResponseEntity.badRequest().body(Map.of("message","You cannot remove your own administrator permission."));

    target.setRole(role);
    users.save(target);
    return ResponseEntity.ok(Map.of("id",target.getId(),"role",target.getRole().name()));
  }
}
