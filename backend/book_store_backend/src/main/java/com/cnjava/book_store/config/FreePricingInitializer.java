package com.cnjava.book_store.config;

import java.math.BigDecimal;
import com.cnjava.book_store.Book.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class FreePricingInitializer implements CommandLineRunner {
  private final BookRepository books;

  public FreePricingInitializer(BookRepository books){this.books=books;}

  @Override
  public void run(String... args){
    for(Book book:books.findAll()){
      if(book.getPrice()==null || book.getPrice().signum()!=0){
        book.setPrice(BigDecimal.ZERO);
        books.save(book);
      }
    }
  }
}
