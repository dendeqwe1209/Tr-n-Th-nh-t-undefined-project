import React,{useState}from'react';

export default function SupportChat(){
  const[open,setOpen]=useState(false),[input,setInput]=useState(''),[busy,setBusy]=useState(false);
  const[messages,setMessages]=useState([{from:'bot',text:'Xin chào! Mình là BookBot. Bạn có thể hỏi về sách, voucher, thanh toán, tài khoản hoặc đơn hàng.'}]);
  const[quick,setQuick]=useState(['Thanh toán thế nào?','Voucher của tôi','Gợi ý sách','Đơn hàng']);

  const send=async text=>{
    const message=(text??input).trim();if(!message||busy)return;
    setMessages(m=>[...m,{from:'user',text:message}]);setInput('');setBusy(true);
    try{
      const res=await fetch('/api/support/chat',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({message})});
      const body=await res.json();
      setMessages(m=>[...m,{from:'bot',text:body.answer||'Mình chưa có câu trả lời phù hợp.'}]);
      if(body.quickReplies)setQuick(body.quickReplies);
    }catch{
      setMessages(m=>[...m,{from:'bot',text:'BookBot đang tạm thời không kết nối được. Hãy thử lại sau.'}]);
    }
    setBusy(false);
  };

  return <>
    <button className="chatLauncher" onClick={()=>setOpen(v=>!v)} aria-label="Customer support">{open?'×':'💬'}</button>
    {open&&<aside className="supportChat">
      <header><div><strong>BookBot</strong><small>Customer support</small></div><button onClick={()=>setOpen(false)}>×</button></header>
      <div className="chatMessages">{messages.map((m,i)=><div key={i} className={'chatBubble '+m.from}>{m.text}</div>)}{busy&&<div className="chatBubble bot">Đang trả lời…</div>}</div>
      <div className="quickReplies">{quick.slice(0,4).map(q=><button key={q} onClick={()=>send(q)}>{q}</button>)}</div>
      <form onSubmit={e=>{e.preventDefault();send()}}><input value={input} onChange={e=>setInput(e.target.value)} placeholder="Nhập câu hỏi..."/><button disabled={busy}>Send</button></form>
    </aside>}
  </>;
}
