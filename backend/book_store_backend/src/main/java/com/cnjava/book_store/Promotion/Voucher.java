package com.cnjava.book_store.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="voucher",uniqueConstraints=@UniqueConstraint(name="uk_voucher_code",columnNames="code"))
public class Voucher {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private long id;
  @Column(nullable=false) private long userId;
  @Column(nullable=false,length=80) private String code;
  @Column(nullable=false,length=140) private String name;
  @Column(nullable=false,length=20) private String discountType;
  @Column(nullable=false,precision=14,scale=2) private BigDecimal value;
  @Column(precision=14,scale=2) private BigDecimal maxDiscount;
  private LocalDateTime expiresAt;
  private LocalDateTime usedAt;
  @Column(nullable=false) private boolean active=true;
  @Column(nullable=false) private boolean welcome=false;
  @Column(nullable=false) private LocalDateTime createdAt;

  @PrePersist public void onCreate(){if(createdAt==null)createdAt=LocalDateTime.now();if(code!=null)code=code.trim().toUpperCase();}

  public long getId(){return id;} public void setId(long v){id=v;}
  public long getUserId(){return userId;} public void setUserId(long v){userId=v;}
  public String getCode(){return code;} public void setCode(String v){code=v;}
  public String getName(){return name;} public void setName(String v){name=v;}
  public String getDiscountType(){return discountType;} public void setDiscountType(String v){discountType=v;}
  public BigDecimal getValue(){return value;} public void setValue(BigDecimal v){value=v;}
  public BigDecimal getMaxDiscount(){return maxDiscount;} public void setMaxDiscount(BigDecimal v){maxDiscount=v;}
  public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime v){expiresAt=v;}
  public LocalDateTime getUsedAt(){return usedAt;} public void setUsedAt(LocalDateTime v){usedAt=v;}
  public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
  public boolean isWelcome(){return welcome;} public void setWelcome(boolean v){welcome=v;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
