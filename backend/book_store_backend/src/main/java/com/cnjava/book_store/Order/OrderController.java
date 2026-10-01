package com.cnjava.book_store.Order;

import java.math.BigDecimal;
import java.util.*;
import com.cnjava.book_store.Auth.*;
import com.cnjava.book_store.Book.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderRepository orders;
  private final BookRepository books;
  private final AuthService auth;

  public OrderController(OrderRepository orders,BookRepository books,AuthService auth){
    this.orders=orders;this.books=books;this.auth=auth;
  }

  public record CheckoutItem(Long bookId,Integer quantity){}
  public record CheckoutRequest(String phone,String shippingAddress,List<CheckoutItem> items){}

  @PostMapping
  @Transactional
  public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request,HttpServletRequest http){
    UserAccount user=auth.currentUser(http).orElse(null);
    if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Please sign in before checkout."));
    if(request.phone()==null||request.phone().isBlank()||request.shippingAddress()==null||request.shippingAddress().isBlank())
      return ResponseEntity.badRequest().body(Map.of("message","Please complete phone and shipping address."));
    if(request.items()==null||request.items().isEmpty())
      return ResponseEntity.badRequest().body(Map.of("message","Cart is empty."));

    List<Book> resolvedBooks=new ArrayList<>();
    List<Integer> quantities=new ArrayList<>();

    for(CheckoutItem item:request.items()){
      if(item.bookId()==null||item.quantity()==null||item.quantity()<1)
        return ResponseEntity.badRequest().body(Map.of("message","Invalid cart item."));
      Optional<Book> maybe=books.findById(item.bookId());
      if(maybe.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message","A book in your cart no longer exists."));
      Book book=maybe.get();
      if(book.getStock()<item.quantity())
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Not enough stock for "+book.getTitle()+". Available: "+book.getStock()));
      resolvedBooks.add(book);
      quantities.add(item.quantity());
    }

    CustomerOrder order=new CustomerOrder();
    order.setUserId(user.getId());
    order.setCustomerName(user.getFullName());
    order.setEmail(user.getEmail());
    order.setPhone(request.phone().trim());
    order.setShippingAddress(request.shippingAddress().trim());
    order.setPaymentMethod("FREE_PAYMENT");
    order.setStatus("PAID");
    order.setTotalAmount(BigDecimal.ZERO);

    for(int i=0;i<resolvedBooks.size();i++){
      Book book=resolvedBooks.get(i);
      int qty=quantities.get(i);
      book.setPrice(BigDecimal.ZERO);
      book.setStock(book.getStock()-qty);
      books.save(book);

      OrderItem orderItem=new OrderItem();
      orderItem.setBookId(book.getId());
      orderItem.setTitle(book.getTitle());
      orderItem.setUnitPrice(BigDecimal.ZERO);
      orderItem.setQuantity(qty);
      orderItem.setSubtotal(BigDecimal.ZERO);
      order.addItem(orderItem);
    }

    CustomerOrder saved=orders.save(order);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> get(@PathVariable long id,HttpServletRequest http){
    UserAccount user=auth.currentUser(http).orElse(null);
    if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Please sign in."));
    CustomerOrder order=orders.findById(id).orElse(null);
    if(order==null) return ResponseEntity.notFound().build();
    if(order.getUserId()==null || order.getUserId()!=user.getId())
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","You cannot access this order."));
    return ResponseEntity.ok(order);
  }
}
