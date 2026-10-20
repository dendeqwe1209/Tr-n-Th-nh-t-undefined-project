package com.cnjava.book_store.Support;

import java.util.*;
import com.cnjava.book_store.Book.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
public class SupportChatController {
  private final BookRepository books;
  public SupportChatController(BookRepository books){this.books=books;}

  public record ChatRequest(String message){}

  @PostMapping("/chat")
  public Map<String,Object> chat(@RequestBody ChatRequest request){
    String raw=request.message()==null?"":request.message().trim();
    String q=raw.toLowerCase(Locale.ROOT);
    String answer;
    List<String> quick=List.of("Thanh toán thế nào?","Voucher của tôi","Gợi ý sách","Đơn hàng");

    if(q.isBlank()) answer="Bạn có thể hỏi mình về sách, tồn kho, voucher, thanh toán sandbox, tài khoản hoặc đơn hàng.";
    else if(has(q,"xin chào","hello","hi ","chào")) answer="Xin chào! Mình là BookBot. Mình có thể hỗ trợ tìm sách, giải thích voucher, checkout và các câu hỏi thường gặp.";
    else if(has(q,"voucher","mã giảm","discount","ưu đãi")) answer="Mỗi tài khoản có một welcome voucher 20% (tối đa 50.000đ), không hết hạn và chỉ dùng một lần. Voucher chỉ bị consume sau khi DemoPay thanh toán thành công. Bạn cũng có thể nhập discount code do admin phát hành.";
    else if(has(q,"thanh toán","payment","card","demopay")) answer="Website dùng DemoPay Sandbox để chạy xuyên suốt luồng thanh toán mà không thu tiền thật. Thẻ test thành công là 4242 4242 4242 4242; thẻ 4000 0000 0000 0002 mô phỏng giao dịch bị từ chối.";
    else if(has(q,"đăng nhập","login","account","tài khoản")) answer="Bạn cần đăng nhập để review và checkout. Admin sử dụng cùng trang login nhưng chỉ tài khoản có role ADMIN mới truy cập được trang quản trị.";
    else if(has(q,"giao hàng","shipping","đơn hàng","order")) answer="Sau khi payment sandbox thành công, đơn được lưu trạng thái PAID. Admin có thể tiếp tục chuyển PROCESSING → SHIPPED → COMPLETED.";
    else if(has(q,"gợi ý","recommend","fantasy","công nghệ","technology","fiction","tiểu thuyết")){
      List<String> picks=books.findAll().stream()
        .filter(b->q.contains("technology")||q.contains("công nghệ")?b.getCategory()!=null&&"Technology".equals(b.getCategory().getName()):true)
        .limit(4).map(Book::getTitle).toList();
      answer=picks.isEmpty()?"Hiện chưa tìm thấy sách phù hợp. Hãy thử tìm theo tên hoặc thể loại.":"Bạn có thể thử: "+String.join(", ",picks)+".";
    }else answer="Mình chưa chắc về câu hỏi này. Bạn có thể hỏi về sách, voucher/mã giảm giá, thanh toán, tài khoản, giao hàng hoặc đơn hàng.";
    return Map.of("answer",answer,"quickReplies",quick);
  }

  private boolean has(String q,String... terms){for(String t:terms)if(q.contains(t))return true;return false;}
}
