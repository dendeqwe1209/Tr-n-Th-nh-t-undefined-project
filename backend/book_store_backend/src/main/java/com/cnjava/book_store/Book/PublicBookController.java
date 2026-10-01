package com.cnjava.book_store.Book;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController
public class PublicBookController {
  private final BookRepository repo; public PublicBookController(BookRepository repo){this.repo=repo;}
  @GetMapping("/api/books") public List<Map<String,Object>> books(){return repo.findAll().stream().map(b->{Map<String,Object> m=new LinkedHashMap<>();m.put("id",b.getId());m.put("title",b.getTitle());m.put("author",b.getAuthor()==null?"Unknown":b.getAuthor().getFullName());m.put("category",b.getCategory()==null?"Uncategorized":b.getCategory().getName());m.put("bookCover",b.getBook_cover());m.put("stock",b.getStock());m.put("description",b.getDescription());return m;}).toList();}
}
