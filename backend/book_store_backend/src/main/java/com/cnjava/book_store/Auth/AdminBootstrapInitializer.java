package com.cnjava.book_store.Auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class AdminBootstrapInitializer implements CommandLineRunner {
  private final UserAccountRepository users;

  @Value("${app.bootstrap-admin.username:}") private String username;
  @Value("${app.bootstrap-admin.password:}") private String password;
  @Value("${app.bootstrap-admin.email:admin@bookstore.local}") private String email;
  @Value("${app.bootstrap-admin.full-name:Bookstore Administrator}") private String fullName;

  public AdminBootstrapInitializer(UserAccountRepository users){this.users=users;}

  @Override
  public void run(String... args){
    if(username==null||username.isBlank()||password==null||password.isBlank()) return;

    UserAccount admin=users.findByUsernameIgnoreCase(username.trim())
      .orElseGet(()->users.findByEmailIgnoreCase(email.trim().toLowerCase()).orElseGet(UserAccount::new));

    admin.setUsername(username.trim());
    admin.setEmail(email.trim().toLowerCase());
    admin.setFullName(fullName==null||fullName.isBlank()?"Bookstore Administrator":fullName.trim());
    admin.setRole(UserRole.ADMIN);
    if(admin.getPasswordHash()==null||!PasswordUtil.matches(password,admin.getPasswordHash())){
      admin.setPasswordHash(PasswordUtil.hash(password));
    }
    users.save(admin);
  }
}
