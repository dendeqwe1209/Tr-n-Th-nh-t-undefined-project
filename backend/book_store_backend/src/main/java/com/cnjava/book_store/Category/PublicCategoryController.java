package com.cnjava.book_store.Category;

import java.util.*;
import com.cnjava.book_store.Book.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class PublicCategoryController {
  private final BookRepository books;

  public PublicCategoryController(BookRepository books){this.books=books;}

  @GetMapping
  public List<Map<String,Object>> list(){
    Map<Long,Map<String,Object>> result=new LinkedHashMap<>();
    books.findAll().stream()
      .filter(b->b.getCategory()!=null)
      .sorted(Comparator.comparing(b->b.getCategory().getName(),String.CASE_INSENSITIVE_ORDER))
      .forEach(b->{
        Category c=b.getCategory();
        result.computeIfAbsent(c.getId(),id->{
          Map<String,Object> m=new LinkedHashMap<>();
          m.put("id",c.getId());m.put("name",c.getName());m.put("bookCount",0L);
          return m;
        });
        Map<String,Object> m=result.get(c.getId());
        m.put("bookCount",((Long)m.get("bookCount"))+1);
      });
    return new ArrayList<>(result.values());
  }
}
