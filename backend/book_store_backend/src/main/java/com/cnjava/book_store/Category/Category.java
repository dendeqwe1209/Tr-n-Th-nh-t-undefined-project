package com.cnjava.book_store.Category;
import java.util.List;
import com.cnjava.book_store.Book.Book;
import jakarta.persistence.*;
@Entity @Table(name="category")
public class Category {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private long id;
  private String name;
  @OneToMany(mappedBy="category",fetch=FetchType.LAZY) private List<Book> books;
  public Category(){} public Category(String name){this.name=name;}
  public long getId(){return id;} public void setId(long id){this.id=id;}
  public String getName(){return name;} public void setName(String name){this.name=name;}
}
