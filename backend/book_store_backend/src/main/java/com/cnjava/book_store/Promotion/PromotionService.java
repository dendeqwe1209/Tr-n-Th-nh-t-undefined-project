package com.cnjava.book_store.Promotion;

import java.math.*;
import java.time.LocalDateTime;
import java.util.*;
import com.cnjava.book_store.Auth.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class PromotionService {
  private final VoucherRepository vouchers;
  private final DiscountCodeRepository codes;

  public PromotionService(VoucherRepository vouchers,DiscountCodeRepository codes){
    this.vouchers=vouchers;this.codes=codes;
  }

  public record PromotionQuote(String kind,Long sourceId,String code,String name,BigDecimal subtotal,BigDecimal discount,BigDecimal total){}

  public Voucher issueWelcomeVoucher(UserAccount user){
    Optional<Voucher> existing=vouchers.findFirstByUserIdAndWelcomeTrue(user.getId());
    if(existing.isPresent()) return existing.get();

    Voucher voucher=new Voucher();
    voucher.setUserId(user.getId());
    voucher.setCode(uniqueVoucherCode("WELCOME20-"+user.getId()));
    voucher.setName("New account 20% off");
    voucher.setDiscountType("PERCENT");
    voucher.setValue(new BigDecimal("20"));
    voucher.setMaxDiscount(new BigDecimal("50000"));
    voucher.setExpiresAt(null);
    voucher.setActive(true);
    voucher.setWelcome(true);
    return vouchers.save(voucher);
  }

  public List<Voucher> activeForUser(long userId){
    LocalDateTime now=LocalDateTime.now();
    return vouchers.findByUserIdOrderByCreatedAtDesc(userId).stream()
      .filter(Voucher::isActive)
      .filter(v->v.getUsedAt()==null)
      .filter(v->v.getExpiresAt()==null||v.getExpiresAt().isAfter(now))
      .toList();
  }

  public PromotionQuote quote(UserAccount user,String rawCode,BigDecimal subtotal){
    BigDecimal safeSubtotal=subtotal==null?BigDecimal.ZERO:subtotal.max(BigDecimal.ZERO);
    String code=rawCode==null?"":rawCode.trim().toUpperCase();
    if(code.isBlank()) return new PromotionQuote("NONE",null,null,null,safeSubtotal,BigDecimal.ZERO,safeSubtotal);

    Voucher voucher=vouchers.findByCodeIgnoreCase(code).orElse(null);
    if(voucher!=null){
      if(voucher.getUserId()!=user.getId()) throw new IllegalArgumentException("This voucher belongs to another account.");
      validateVoucher(voucher);
      BigDecimal discount=calculate(voucher.getDiscountType(),voucher.getValue(),voucher.getMaxDiscount(),safeSubtotal);
      return new PromotionQuote("VOUCHER",voucher.getId(),voucher.getCode(),voucher.getName(),safeSubtotal,discount,safeSubtotal.subtract(discount));
    }

    DiscountCode discountCode=codes.findByCodeIgnoreCase(code).orElse(null);
    if(discountCode!=null){
      validateCode(discountCode);
      BigDecimal discount=calculate(discountCode.getDiscountType(),discountCode.getValue(),discountCode.getMaxDiscount(),safeSubtotal);
      return new PromotionQuote("CODE",discountCode.getId(),discountCode.getCode(),discountCode.getName(),safeSubtotal,discount,safeSubtotal.subtract(discount));
    }

    throw new IllegalArgumentException("Voucher or discount code not found.");
  }

  public void consume(PromotionQuote quote){
    if(quote==null||quote.sourceId()==null) return;
    if("VOUCHER".equals(quote.kind())){
      Voucher voucher=vouchers.findById(quote.sourceId()).orElse(null);
      if(voucher!=null){
        voucher.setUsedAt(LocalDateTime.now());
        voucher.setActive(false);
        vouchers.save(voucher);
      }
    }else if("CODE".equals(quote.kind())){
      DiscountCode code=codes.findById(quote.sourceId()).orElse(null);
      if(code!=null){
        code.setUsedCount(code.getUsedCount()+1);
        codes.save(code);
      }
    }
  }

  private void validateVoucher(Voucher v){
    if(!v.isActive()||v.getUsedAt()!=null) throw new IllegalArgumentException("This voucher has already been used or is inactive.");
    if(v.getExpiresAt()!=null&&!v.getExpiresAt().isAfter(LocalDateTime.now())) throw new IllegalArgumentException("This voucher has expired.");
  }

  private void validateCode(DiscountCode c){
    if(!c.isActive()) throw new IllegalArgumentException("This discount code is inactive.");
    if(c.getExpiresAt()!=null&&!c.getExpiresAt().isAfter(LocalDateTime.now())) throw new IllegalArgumentException("This discount code has expired.");
    if(c.getUsageLimit()!=null&&c.getUsageLimit()>0&&c.getUsedCount()>=c.getUsageLimit()) throw new IllegalArgumentException("This discount code has reached its usage limit.");
  }

  private BigDecimal calculate(String type,BigDecimal value,BigDecimal maxDiscount,BigDecimal subtotal){
    BigDecimal discount;
    if("FIXED".equalsIgnoreCase(type)){
      discount=value==null?BigDecimal.ZERO:value;
    }else{
      BigDecimal pct=value==null?BigDecimal.ZERO:value;
      discount=subtotal.multiply(pct).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
    }
    if(maxDiscount!=null&&maxDiscount.signum()>0&&discount.compareTo(maxDiscount)>0) discount=maxDiscount;
    if(discount.signum()<0) discount=BigDecimal.ZERO;
    if(discount.compareTo(subtotal)>0) discount=subtotal;
    return discount.setScale(2,RoundingMode.HALF_UP);
  }

  private String uniqueVoucherCode(String prefix){
    String candidate;
    do{
      candidate=(prefix+"-"+UUID.randomUUID().toString().substring(0,6)).toUpperCase();
    }while(vouchers.existsByCodeIgnoreCase(candidate));
    return candidate;
  }
}
