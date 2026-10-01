package com.cnjava.book_store.Order;

import java.math.BigDecimal;
import java.util.*;
import com.cnjava.book_store.Book.*;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderRepository orders;
  private final BookRepository books;
  public OrderController(OrderRepository orders,BookRepository books){this.orders=orders;this.books=books;}

  public record CheckoutItem(Long bookId,Integer quantity){}
  public record CheckoutRequest(String customerName,String email,String phone,String shippingAddress,String paymentMethod,List<CheckoutItem> items){}

  @PostMapping
  @Transactional
  public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request){
    if(request.customerName()==null||request.customerName().isBlank()||
       request.email()==null||request.email().isBlank()||
       request.phone()==null||request.phone().isBlank()||
       request.shippingAddress()==null||request.shippingAddress().isBlank())
      return ResponseEntity.badRequest().body(Map.of("message","Please complete all shipping fields."));
    if(request.items()==null||request.items().isEmpty())
      return ResponseEntity.badRequest().body(Map.of("message","Cart is empty."));

    List<Book> resolvedBooks=new ArrayList<>();
    List<Integer> quantities=new ArrayList<>();
    BigDecimal total=BigDecimal.ZERO;

    for(CheckoutItem item:request.items()){
      if(item.bookId()==null||item.quantity()==null||item.quantity()<1)
        return ResponseEntity.badRequest().body(Map.of("message","Invalid cart item."));
      Optional<Book> maybe=books.findById(item.bookId());
      if(maybe.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message","A book in your cart no longer exists."));
      Book book=maybe.get();
      if(book.getStock()<item.quantity())
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Not enough stock for "+book.getTitle()+". Available: "+book.getStock()));
      BigDecimal price=book.getPrice()==null?BigDecimal.ZERO:book.getPrice();
      total=total.add(price.multiply(BigDecimal.valueOf(item.quantity())));
      resolvedBooks.add(book);
      quantities.add(item.quantity());
    }

    CustomerOrder order=new CustomerOrder();
    order.setCustomerName(request.customerName().trim());
    order.setEmail(request.email().trim());
    order.setPhone(request.phone().trim());
    order.setShippingAddress(request.shippingAddress().trim());
    order.setPaymentMethod(request.paymentMethod()==null||request.paymentMethod().isBlank()?"COD":request.paymentMethod().trim().toUpperCase());
    order.setStatus("PLACED");
    order.setTotalAmount(total);

    for(int i=0;i<resolvedBooks.size();i++){
      Book book=resolvedBooks.get(i);
      int qty=quantities.get(i);
      BigDecimal price=book.getPrice()==null?BigDecimal.ZERO:book.getPrice();
      book.setStock(book.getStock()-qty);
      books.save(book);
      OrderItem orderItem=new OrderItem();
      orderItem.setBookId(book.getId());
      orderItem.setTitle(book.getTitle());
      orderItem.setUnitPrice(price);
      orderItem.setQuantity(qty);
      orderItem.setSubtotal(price.multiply(BigDecimal.valueOf(qty)));
      order.addItem(orderItem);
    }

    CustomerOrder saved=orders.save(order);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerOrder> get(@PathVariable long id){
    return orders.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }
}
