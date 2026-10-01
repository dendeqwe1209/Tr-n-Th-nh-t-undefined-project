package com.cnjava.book_store.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name="customer_order")
public class CustomerOrder {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private long id;
  private Long userId;
  @Column(nullable=false,length=120) private String customerName;
  @Column(nullable=false,length=180) private String email;
  @Column(nullable=false,length=40) private String phone;
  @Column(nullable=false,length=500) private String shippingAddress;
  @Column(nullable=false,length=40) private String paymentMethod;
  @Column(nullable=false,length=40) private String status;
  @Column(nullable=false,precision=14,scale=2) private BigDecimal totalAmount;
  @Column(nullable=false) private LocalDateTime createdAt;
  @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER)
  private List<OrderItem> items=new ArrayList<>();

  @PrePersist public void onCreate(){if(createdAt==null)createdAt=LocalDateTime.now();if(status==null)status="PAID";}
  public void addItem(OrderItem item){items.add(item);item.setOrder(this);}
  public long getId(){return id;} public void setId(long id){this.id=id;}
  public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
  public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
  public String getEmail(){return email;} public void setEmail(String v){email=v;}
  public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
  public String getShippingAddress(){return shippingAddress;} public void setShippingAddress(String v){shippingAddress=v;}
  public String getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(String v){paymentMethod=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
  public List<OrderItem> getItems(){return items;} public void setItems(List<OrderItem> v){items=v;}
}
