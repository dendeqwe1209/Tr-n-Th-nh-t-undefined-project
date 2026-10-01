package com.cnjava.book_store.Book;

import java.math.BigDecimal;
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
 private String book_cover;
 private int stock;
 @Column(length=2000) private String description;
 @Column(length=5000) private String previewText;
 private boolean featured;
 @Column(precision=14,scale=2) private BigDecimal price = BigDecimal.ZERO;

 public Book(){}
 public Book(long id,String title,Author author,Category category,String book_cover,int stock,String description){
   this.id=id;this.title=title;this.author=author;this.category=category;this.book_cover=book_cover;this.stock=stock;this.description=description;
 }
 public long getId(){return id;} public void setId(long id){this.id=id;}
 public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
 public Author getAuthor(){return author;} public void setAuthor(Author author){this.author=author;}
 public Category getCategory(){return category;} public void setCategory(Category category){this.category=category;}
 public String getBook_cover(){return book_cover;} public void setBook_cover(String book_cover){this.book_cover=book_cover;}
 public int getStock(){return stock;} public void setStock(int stock){this.stock=stock;}
 public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
 public String getPreviewText(){return previewText;} public void setPreviewText(String previewText){this.previewText=previewText;}
 public boolean isFeatured(){return featured;} public void setFeatured(boolean featured){this.featured=featured;}
 public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal price){this.price=price;}
}
