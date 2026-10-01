package com.cnjava.book_store.Book;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PublicBookController {
  private final BookRepository repo;
  public PublicBookController(BookRepository repo){this.repo=repo;}

  @GetMapping("/api/books")
  public List<Map<String,Object>> books(){
    return repo.findAll().stream().map(this::toMap).toList();
  }

  @GetMapping("/api/books/{id}")
  public ResponseEntity<Map<String,Object>> book(@PathVariable Long id){
    return repo.findById(id).map(b->ResponseEntity.ok(toMap(b))).orElse(ResponseEntity.notFound().build());
  }

  private Map<String,Object> toMap(Book b){
    Map<String,Object> m=new LinkedHashMap<>();
    m.put("id",b.getId());
    m.put("title",b.getTitle());
    m.put("author",b.getAuthor()==null?"Unknown":b.getAuthor().getFullName());
    m.put("category",b.getCategory()==null?"Uncategorized":b.getCategory().getName());
    m.put("bookCover",b.getBook_cover());
    m.put("stock",b.getStock());
    m.put("description",b.getDescription());
    m.put("price",b.getPrice()==null?BigDecimal.ZERO:b.getPrice());
    return m;
  }
}
