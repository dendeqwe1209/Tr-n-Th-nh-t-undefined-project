package com.cnjava.book_store.config;

import java.math.BigDecimal;
import com.cnjava.book_store.Author.*;
import com.cnjava.book_store.Book.*;
import com.cnjava.book_store.Category.*;
import com.cnjava.book_store.Staff.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {
  private final BookRepository books; private final AuthorRepository authors; private final CategoryRepository categories; private final StaffRepository staff;
  @Value("${app.seed-demo:false}") private boolean enabled;
  public DemoDataInitializer(BookRepository books,AuthorRepository authors,CategoryRepository categories,StaffRepository staff){this.books=books;this.authors=authors;this.categories=categories;this.staff=staff;}

  @Override public void run(String... args){
    if(!enabled) return;
    if(books.count()==0){
      Author a1=authors.save(new Author(0,"George Orwell")); Author a2=authors.save(new Author(0,"Haruki Murakami")); Author a3=authors.save(new Author(0,"Yuval Noah Harari"));
      Category fiction=categories.save(new Category("Fiction")); Category history=categories.save(new Category("History"));
      books.save(book("1984",a1,fiction,"https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg",12,"A dystopian classic about surveillance, language and power.",149000));
      books.save(book("Norwegian Wood",a2,fiction,"https://covers.openlibrary.org/b/isbn/9780375704024-L.jpg",8,"A reflective novel about memory, youth and loss.",179000));
      books.save(book("Kafka on the Shore",a2,fiction,"https://covers.openlibrary.org/b/isbn/9781400079278-L.jpg",6,"A surreal journey blending fate, identity and imagination.",189000));
      books.save(book("Sapiens",a3,history,"https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg",10,"A broad history of humankind and the ideas shaping society.",229000));
    } else {
      for(Book b:books.findAll()){
        if(b.getPrice()==null || b.getPrice().signum()==0){
          long price=switch(b.getTitle()){
            case "1984" -> 149000L;
            case "Norwegian Wood" -> 179000L;
            case "Kafka on the Shore" -> 189000L;
            case "Sapiens" -> 229000L;
            default -> 129000L;
          };
          b.setPrice(BigDecimal.valueOf(price));
          books.save(b);
        }
      }
    }
    if(staff.count()==0) staff.save(new Staff("Demo Admin","admin@bookstore.demo","0900000000"));
  }

  private Book book(String title,Author author,Category category,String cover,int stock,String description,long price){
    Book b=new Book(0,title,author,category,cover,stock,description); b.setPrice(BigDecimal.valueOf(price)); return b;
  }
}
