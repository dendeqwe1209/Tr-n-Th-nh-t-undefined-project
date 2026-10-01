package com.cnjava.book_store.Admin;

import java.math.BigDecimal;
import java.util.*;
import com.cnjava.book_store.Author.*;
import com.cnjava.book_store.Auth.*;
import com.cnjava.book_store.Book.*;
import com.cnjava.book_store.Category.*;
import com.cnjava.book_store.Order.*;
import com.cnjava.book_store.Review.*;
import com.cnjava.book_store.Staff.*;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final BookRepository books;
  private final AuthorRepository authors;
  private final CategoryRepository categories;
  private final UserAccountRepository users;
  private final OrderRepository orders;
  private final ReviewRepository reviews;
  private final StaffRepository staff;

  public AdminController(
    BookRepository books,AuthorRepository authors,CategoryRepository categories,
    UserAccountRepository users,OrderRepository orders,ReviewRepository reviews,StaffRepository staff
  ){
    this.books=books;this.authors=authors;this.categories=categories;
    this.users=users;this.orders=orders;this.reviews=reviews;this.staff=staff;
  }

  public record BookRequest(String title,String author,String category,String bookCover,Integer stock,String description,String previewText,Boolean featured){}
  public record CategoryRequest(String name){}
  public record OrderStatusRequest(String status){}

  @GetMapping("/overview")
  public Map<String,Object> overview(){
    Map<String,Object> body=new LinkedHashMap<>();
    body.put("books",books.count());
    body.put("categories",categories.count());
    body.put("users",users.count());
    body.put("orders",orders.count());
    body.put("reviews",reviews.count());
    body.put("staff",staff.count());
    body.put("stock",books.findAll().stream().mapToInt(Book::getStock).sum());
    return body;
  }

  @GetMapping("/books")
  public List<Map<String,Object>> bookList(){
    return books.findAll().stream()
      .sorted(Comparator.comparing(Book::getTitle,String.CASE_INSENSITIVE_ORDER))
      .map(this::bookMap).toList();
  }

  @PostMapping("/books")
  public ResponseEntity<?> createBook(@RequestBody BookRequest request){
    String error=validateBook(request);
    if(error!=null) return ResponseEntity.badRequest().body(Map.of("message",error));
    if(books.existsByTitleIgnoreCase(request.title().trim()))
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","A book with this title already exists."));
    Book book=new Book();
    applyBook(book,request);
    return ResponseEntity.status(HttpStatus.CREATED).body(bookMap(books.save(book)));
  }

  @PutMapping("/books/{id}")
  public ResponseEntity<?> updateBook(@PathVariable long id,@RequestBody BookRequest request){
    Book book=books.findById(id).orElse(null);
    if(book==null) return ResponseEntity.notFound().build();
    String error=validateBook(request);
    if(error!=null) return ResponseEntity.badRequest().body(Map.of("message",error));
    applyBook(book,request);
    return ResponseEntity.ok(bookMap(books.save(book)));
  }

  @DeleteMapping("/books/{id}")
  @Transactional
  public ResponseEntity<?> deleteBook(@PathVariable long id){
    if(!books.existsById(id)) return ResponseEntity.notFound().build();
    reviews.deleteByBookId(id);
    books.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/categories")
  public List<Map<String,Object>> categoryList(){
    Map<Long,Long> counts=new HashMap<>();
    for(Book book:books.findAll()){
      if(book.getCategory()!=null) counts.merge(book.getCategory().getId(),1L,Long::sum);
    }
    return categories.findAll().stream()
      .sorted(Comparator.comparing(Category::getName,String.CASE_INSENSITIVE_ORDER))
      .map(c->Map.<String,Object>of("id",c.getId(),"name",c.getName(),"bookCount",counts.getOrDefault(c.getId(),0L)))
      .toList();
  }

  @PostMapping("/categories")
  public ResponseEntity<?> createCategory(@RequestBody CategoryRequest request){
    String name=request.name()==null?"":request.name().trim();
    if(name.length()<2) return ResponseEntity.badRequest().body(Map.of("message","Category name is required."));
    if(categories.findByNameIgnoreCase(name).isPresent())
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Category already exists."));
    return ResponseEntity.status(HttpStatus.CREATED).body(categories.save(new Category(name)));
  }

  @PutMapping("/categories/{id}")
  public ResponseEntity<?> updateCategory(@PathVariable long id,@RequestBody CategoryRequest request){
    Category category=categories.findById(id).orElse(null);
    if(category==null) return ResponseEntity.notFound().build();
    String name=request.name()==null?"":request.name().trim();
    if(name.length()<2) return ResponseEntity.badRequest().body(Map.of("message","Category name is required."));
    category.setName(name);
    categories.save(category);
    return ResponseEntity.ok(Map.of("id",category.getId(),"name",category.getName()));
  }

  @DeleteMapping("/categories/{id}")
  public ResponseEntity<?> deleteCategory(@PathVariable long id){
    if(!categories.existsById(id)) return ResponseEntity.notFound().build();
    if(books.existsByCategory_Id(id))
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Move or delete books in this category first."));
    categories.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/users")
  public List<Map<String,Object>> userList(){
    return users.findAll().stream().map(u->{
      Map<String,Object> m=new LinkedHashMap<>();
      m.put("id",u.getId());m.put("fullName",u.getFullName());m.put("email",u.getEmail());m.put("createdAt",u.getCreatedAt());
      return m;
    }).toList();
  }

  @GetMapping("/orders")
  public List<CustomerOrder> orderList(){return orders.findAllByOrderByCreatedAtDesc();}

  @PutMapping("/orders/{id}/status")
  public ResponseEntity<?> orderStatus(@PathVariable long id,@RequestBody OrderStatusRequest request){
    CustomerOrder order=orders.findById(id).orElse(null);
    if(order==null) return ResponseEntity.notFound().build();
    String status=request.status()==null?"":request.status().trim().toUpperCase();
    if(!Set.of("PAID","PROCESSING","SHIPPED","COMPLETED","CANCELLED").contains(status))
      return ResponseEntity.badRequest().body(Map.of("message","Unsupported order status."));
    order.setStatus(status);
    return ResponseEntity.ok(orders.save(order));
  }

  @GetMapping("/reviews")
  public List<Map<String,Object>> reviewList(){
    Map<Long,String> titles=new HashMap<>();
    for(Book b:books.findAll()) titles.put(b.getId(),b.getTitle());
    return reviews.findAll().stream()
      .sorted(Comparator.comparing(Review::getCreatedAt,Comparator.nullsLast(Comparator.reverseOrder())))
      .map(r->{
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("id",r.getId());m.put("bookId",r.getBookId());m.put("bookTitle",titles.getOrDefault(r.getBookId(),"Deleted book"));
        m.put("reviewerName",r.getReviewerName());m.put("rating",r.getRating());m.put("comment",r.getComment());m.put("createdAt",r.getCreatedAt());
        return m;
      }).toList();
  }

  @DeleteMapping("/reviews/{id}")
  public ResponseEntity<?> deleteReview(@PathVariable long id){
    if(!reviews.existsById(id)) return ResponseEntity.notFound().build();
    reviews.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private String validateBook(BookRequest r){
    if(r.title()==null||r.title().trim().length()<1) return "Title is required.";
    if(r.author()==null||r.author().trim().length()<2) return "Author is required.";
    if(r.category()==null||r.category().trim().length()<2) return "Category is required.";
    if(r.stock()==null||r.stock()<0) return "Stock must be zero or greater.";
    return null;
  }

  private void applyBook(Book book,BookRequest r){
    Author author=authors.findByFullNameIgnoreCase(r.author().trim()).orElseGet(()->authors.save(new Author(0,r.author().trim())));
    Category category=categories.findByNameIgnoreCase(r.category().trim()).orElseGet(()->categories.save(new Category(r.category().trim())));
    book.setTitle(r.title().trim());
    book.setAuthor(author);
    book.setCategory(category);
    book.setBook_cover(r.bookCover()==null||r.bookCover().isBlank()?categoryCover(category.getName()):r.bookCover().trim());
    book.setStock(r.stock());
    book.setDescription(r.description()==null?"":r.description().trim());
    book.setPreviewText(r.previewText()==null?"":r.previewText().trim());
    book.setFeatured(Boolean.TRUE.equals(r.featured()));
    book.setPrice(BigDecimal.ZERO);
  }

  private Map<String,Object> bookMap(Book b){
    Map<String,Object> m=new LinkedHashMap<>();
    m.put("id",b.getId());m.put("title",b.getTitle());
    m.put("author",b.getAuthor()==null?"Unknown":b.getAuthor().getFullName());
    m.put("category",b.getCategory()==null?"Uncategorized":b.getCategory().getName());
    m.put("bookCover",b.getBook_cover());m.put("stock",b.getStock());m.put("description",b.getDescription());m.put("previewText",b.getPreviewText());m.put("featured",b.isFeatured());m.put("price",BigDecimal.ZERO);
    return m;
  }

  private String categoryCover(String category){
    return switch(category){
      case "Classics" -> "/images/categories/classics.svg";
      case "Fiction" -> "/images/categories/fiction.svg";
      case "Fantasy" -> "/images/categories/fantasy.svg";
      case "Science Fiction" -> "/images/categories/science-fiction.svg";
      case "Mystery & Thriller" -> "/images/categories/mystery-thriller.svg";
      case "Romance" -> "/images/categories/romance.svg";
      case "History & Biography" -> "/images/categories/history-biography.svg";
      case "Self-Help & Business" -> "/images/categories/self-help-business.svg";
      case "Technology" -> "/images/categories/technology.svg";
      case "Philosophy" -> "/images/categories/philosophy.svg";
      default -> "/images/categories/fiction.svg";
    };
  }
}
