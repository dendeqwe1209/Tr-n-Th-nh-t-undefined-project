package com.cnjava.book_store.Order;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name="order_item")
public class OrderItem {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private long id;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id",nullable=false) @JsonIgnore private CustomerOrder order;
  @Column(nullable=false) private long bookId;
  @Column(nullable=false,length=255) private String title;
  @Column(nullable=false,precision=14,scale=2) private BigDecimal unitPrice;
  @Column(nullable=false) private int quantity;
  @Column(nullable=false,precision=14,scale=2) private BigDecimal subtotal;

  public long getId(){return id;} public void setId(long id){this.id=id;}
  public CustomerOrder getOrder(){return order;} public void setOrder(CustomerOrder v){order=v;}
  public long getBookId(){return bookId;} public void setBookId(long v){bookId=v;}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){unitPrice=v;}
  public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
  public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;}
}
