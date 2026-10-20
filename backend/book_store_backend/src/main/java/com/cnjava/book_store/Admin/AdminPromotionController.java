package com.cnjava.book_store.Admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import com.cnjava.book_store.Auth.*;
import com.cnjava.book_store.Promotion.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/promotions")
public class AdminPromotionController {
  private final VoucherRepository vouchers;
  private final DiscountCodeRepository codes;
  private final UserAccountRepository users;

  public AdminPromotionController(VoucherRepository vouchers,DiscountCodeRepository codes,UserAccountRepository users){
    this.vouchers=vouchers;this.codes=codes;this.users=users;
  }

  public record VoucherRequest(Long userId,String code,String name,String discountType,BigDecimal value,BigDecimal maxDiscount,LocalDateTime expiresAt,Boolean active){}
  public record CodeRequest(String code,String name,String discountType,BigDecimal value,BigDecimal maxDiscount,LocalDateTime expiresAt,Integer usageLimit,Boolean active){}

  @GetMapping("/vouchers")
  public List<Voucher> vouchers(){return vouchers.findAll().stream().sorted(Comparator.comparing(Voucher::getCreatedAt).reversed()).toList();}

  @PostMapping("/vouchers")
  public ResponseEntity<?> createVoucher(@RequestBody VoucherRequest r){
    String error=validate(r.discountType(),r.value()); if(error!=null)return ResponseEntity.badRequest().body(Map.of("message",error));
    if(r.userId()==null||!users.existsById(r.userId())) return ResponseEntity.badRequest().body(Map.of("message","Select a valid account."));
    String code=(r.code()==null||r.code().isBlank())?"VOUCHER-"+UUID.randomUUID().toString().substring(0,8):r.code().trim().toUpperCase();
    if(vouchers.existsByCodeIgnoreCase(code)||codes.existsByCodeIgnoreCase(code)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Promotion code already exists."));
    Voucher v=new Voucher();v.setUserId(r.userId());v.setCode(code);v.setName(name(r.name(),"Account voucher"));
    v.setDiscountType(type(r.discountType()));v.setValue(r.value());v.setMaxDiscount(r.maxDiscount());v.setExpiresAt(r.expiresAt());v.setActive(r.active()==null||r.active());v.setWelcome(false);
    return ResponseEntity.status(HttpStatus.CREATED).body(vouchers.save(v));
  }

  @PutMapping("/vouchers/{id}")
  public ResponseEntity<?> updateVoucher(@PathVariable long id,@RequestBody VoucherRequest r){
    Voucher v=vouchers.findById(id).orElse(null);if(v==null)return ResponseEntity.notFound().build();
    String error=validate(r.discountType(),r.value());if(error!=null)return ResponseEntity.badRequest().body(Map.of("message",error));
    v.setName(name(r.name(),v.getName()));v.setDiscountType(type(r.discountType()));v.setValue(r.value());v.setMaxDiscount(r.maxDiscount());
    if(!v.isWelcome())v.setExpiresAt(r.expiresAt());
    if(r.active()!=null)v.setActive(r.active());
    return ResponseEntity.ok(vouchers.save(v));
  }

  @GetMapping("/codes")
  public List<DiscountCode> codes(){return codes.findAll().stream().sorted(Comparator.comparing(DiscountCode::getCreatedAt).reversed()).toList();}

  @PostMapping("/codes")
  public ResponseEntity<?> createCode(@RequestBody CodeRequest r){
    String error=validate(r.discountType(),r.value());if(error!=null)return ResponseEntity.badRequest().body(Map.of("message",error));
    String code=(r.code()==null?"":r.code().trim().toUpperCase());
    if(code.length()<3)return ResponseEntity.badRequest().body(Map.of("message","Discount code must contain at least 3 characters."));
    if(vouchers.existsByCodeIgnoreCase(code)||codes.existsByCodeIgnoreCase(code)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Promotion code already exists."));
    if(r.usageLimit()!=null&&r.usageLimit()<1)return ResponseEntity.badRequest().body(Map.of("message","Usage limit must be at least 1."));
    DiscountCode c=new DiscountCode();c.setCode(code);c.setName(name(r.name(),"Discount code"));c.setDiscountType(type(r.discountType()));
    c.setValue(r.value());c.setMaxDiscount(r.maxDiscount());c.setExpiresAt(r.expiresAt());c.setUsageLimit(r.usageLimit());c.setActive(r.active()==null||r.active());
    return ResponseEntity.status(HttpStatus.CREATED).body(codes.save(c));
  }

  @PutMapping("/codes/{id}")
  public ResponseEntity<?> updateCode(@PathVariable long id,@RequestBody CodeRequest r){
    DiscountCode c=codes.findById(id).orElse(null);if(c==null)return ResponseEntity.notFound().build();
    String error=validate(r.discountType(),r.value());if(error!=null)return ResponseEntity.badRequest().body(Map.of("message",error));
    if(r.usageLimit()!=null&&r.usageLimit()<1)return ResponseEntity.badRequest().body(Map.of("message","Usage limit must be at least 1."));
    c.setName(name(r.name(),c.getName()));c.setDiscountType(type(r.discountType()));c.setValue(r.value());c.setMaxDiscount(r.maxDiscount());
    c.setExpiresAt(r.expiresAt());c.setUsageLimit(r.usageLimit());if(r.active()!=null)c.setActive(r.active());
    return ResponseEntity.ok(codes.save(c));
  }

  private String validate(String t,BigDecimal value){
    String type=type(t);if(!Set.of("PERCENT","FIXED").contains(type))return "Discount type must be PERCENT or FIXED.";
    if(value==null||value.signum()<=0)return "Discount value must be greater than zero.";
    if("PERCENT".equals(type)&&value.compareTo(new BigDecimal("100"))>0)return "Percent discount cannot exceed 100.";
    return null;
  }
  private String type(String t){return t==null?"":t.trim().toUpperCase();}
  private String name(String value,String fallback){return value==null||value.isBlank()?fallback:value.trim();}
}
