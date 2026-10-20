package com.cnjava.book_store.Order;

import java.math.*;
import java.util.*;
import com.cnjava.book_store.Auth.*;
import com.cnjava.book_store.Book.*;
import com.cnjava.book_store.Payment.PaymentSandboxService;
import com.cnjava.book_store.Promotion.PromotionService;
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
  private final PromotionService promotions;
  private final PaymentSandboxService payments;

  public OrderController(OrderRepository orders,BookRepository books,AuthService auth,PromotionService promotions,PaymentSandboxService payments){
    this.orders=orders;this.books=books;this.auth=auth;this.promotions=promotions;this.payments=payments;
  }

  public record CheckoutItem(Long bookId,Integer quantity){}
  public record QuoteRequest(List<CheckoutItem> items,String promotionCode){}
  public record CheckoutRequest(String phone,String shippingAddress,List<CheckoutItem> items,String promotionCode,String paymentMethod,String paymentCode){}
  private record CartLine(Book book,int quantity,BigDecimal subtotal){}

  @PostMapping("/quote")
  public ResponseEntity<?> quote(@RequestBody QuoteRequest request,HttpServletRequest http){
    UserAccount user=auth.currentUser(http).orElse(null);
    if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Please sign in before checkout."));
    try{
      List<CartLine> lines=resolve(request.items(),false);
      BigDecimal subtotal=subtotal(lines);
      PromotionService.PromotionQuote promo=promotions.quote(user,request.promotionCode(),subtotal);
      return ResponseEntity.ok(quoteBody(promo,lines));
    }catch(IllegalArgumentException e){
      return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
    }
  }

  @PostMapping
  @Transactional
  public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request,HttpServletRequest http){
    UserAccount user=auth.currentUser(http).orElse(null);
    if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Please sign in before checkout."));
    if(request.phone()==null||request.phone().isBlank()||request.shippingAddress()==null||request.shippingAddress().isBlank())
      return ResponseEntity.badRequest().body(Map.of("message","Please complete phone and shipping address."));

    try{
      List<CartLine> lines=resolve(request.items(),true);
      BigDecimal subtotal=subtotal(lines);
      PromotionService.PromotionQuote promo=promotions.quote(user,request.promotionCode(),subtotal);
      PaymentSandboxService.PaymentResult payment=payments.authorize(request.paymentMethod(),request.paymentCode(),promo.total());
      if(!payment.approved()) return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of("message",payment.message()));

      CustomerOrder order=new CustomerOrder();
      order.setUserId(user.getId());order.setCustomerName(user.getFullName());order.setEmail(user.getEmail());
      order.setPhone(request.phone().trim());order.setShippingAddress(request.shippingAddress().trim());
      order.setPaymentMethod("DEMO_CARD");order.setPaymentReference(payment.reference());order.setStatus("PAID");
      order.setSubtotalAmount(promo.subtotal());order.setDiscountAmount(promo.discount());order.setTotalAmount(promo.total());order.setPromotionCode(promo.code());

      for(CartLine line:lines){
        Book book=line.book();book.setStock(book.getStock()-line.quantity());books.save(book);
        OrderItem item=new OrderItem();item.setBookId(book.getId());item.setTitle(book.getTitle());item.setUnitPrice(book.getPrice());
        item.setQuantity(line.quantity());item.setSubtotal(line.subtotal());order.addItem(item);
      }

      CustomerOrder saved=orders.save(order);
      promotions.consume(promo);
      return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }catch(IllegalArgumentException e){
      return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> get(@PathVariable long id,HttpServletRequest http){
    UserAccount user=auth.currentUser(http).orElse(null);
    if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Please sign in."));
    CustomerOrder order=orders.findById(id).orElse(null);
    if(order==null) return ResponseEntity.notFound().build();
    if(user.getRole()!=UserRole.ADMIN && (order.getUserId()==null || order.getUserId()!=user.getId()))
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","You cannot access this order."));
    return ResponseEntity.ok(order);
  }

  private List<CartLine> resolve(List<CheckoutItem> items,boolean checkStock){
    if(items==null||items.isEmpty()) throw new IllegalArgumentException("Cart is empty.");
    List<CartLine> lines=new ArrayList<>();
    for(CheckoutItem item:items){
      if(item.bookId()==null||item.quantity()==null||item.quantity()<1) throw new IllegalArgumentException("Invalid cart item.");
      Book book=books.findById(item.bookId()).orElseThrow(()->new IllegalArgumentException("A book in your cart no longer exists."));
      if(checkStock&&book.getStock()<item.quantity()) throw new IllegalArgumentException("Not enough stock for "+book.getTitle()+". Available: "+book.getStock());
      BigDecimal price=book.getPrice()==null?BigDecimal.ZERO:book.getPrice();
      lines.add(new CartLine(book,item.quantity(),price.multiply(BigDecimal.valueOf(item.quantity()))));
    }
    return lines;
  }

  private BigDecimal subtotal(List<CartLine> lines){
    return lines.stream().map(CartLine::subtotal).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,RoundingMode.HALF_UP);
  }

  private Map<String,Object> quoteBody(PromotionService.PromotionQuote promo,List<CartLine> lines){
    Map<String,Object> body=new LinkedHashMap<>();
    body.put("itemCount",lines.stream().mapToInt(CartLine::quantity).sum());
    body.put("subtotal",promo.subtotal());body.put("discount",promo.discount());body.put("total",promo.total());
    body.put("promotionCode",promo.code());body.put("promotionName",promo.name());body.put("promotionKind",promo.kind());
    return body;
  }
}
