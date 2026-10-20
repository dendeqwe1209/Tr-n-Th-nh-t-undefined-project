package com.cnjava.book_store.Promotion;

import com.cnjava.book_store.Auth.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class PromotionInitializer implements CommandLineRunner {
  private final UserAccountRepository users;
  private final PromotionService promotions;

  public PromotionInitializer(UserAccountRepository users,PromotionService promotions){this.users=users;this.promotions=promotions;}

  @Override
  public void run(String... args){
    for(UserAccount user:users.findAll()) promotions.issueWelcomeVoucher(user);
  }
}
