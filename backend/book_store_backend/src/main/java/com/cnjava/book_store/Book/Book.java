package com.cnjava.book_store.Book;

import com.cnjava.book_store.Author.Author;
import com.cnjava.book_store.Category.Category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name="book")
public class Book {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private long id;
 @Column(nullable=false,length=255) private String title;
 @JoinColumn(name="author_id") @JsonIgnore @ManyToOne(fetch=FetchType.LAZY) private Author author;
 @ManyToOne(fetch=FetchType.LAZY) @JsonIgnore @JoinColumn(name="category_id") private Category category;
 private String book_cover; private int stock; private String description;
 public Book(){}
 public Book(long id,String title,Author author,Category category,String book_cover,int stock,String description){this.id=id;this.title=title;this.author=author;this.category=category;this.book_cover=book_cover;this.stock=stock;this.description=description;}
 public long getId(){return id;} public void setId(long id){this.id=id;} public String getTitle(){return title;} public void setTitle(String title){this.title=title;} public Author getAuthor(){return author;} public void setAuthor(Author author){this.author=author;} public Category getCategory(){return category;} public void setCategory(Category category){this.category=category;} public String getBook_cover(){return book_cover;} public void setBook_cover(String book_cover){this.book_cover=book_cover;} public int getStock(){return stock;} public void setStock(int stock){this.stock=stock;} public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
}
