import React,{useMemo,useState}from'react';

const readCart=()=>{try{return JSON.parse(localStorage.getItem('book-store-cart')||'[]')}catch{return[]}};
const money=v=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(v||0));

export default function Checkout(){
  const[cart,setCart]=useState(readCart),[form,setForm]=useState({customerName:'',email:'',phone:'',shippingAddress:'',paymentMethod:'COD'}),[busy,setBusy]=useState(false),[error,setError]=useState(''),[order,setOrder]=useState(null);
  const total=useMemo(()=>cart.reduce((sum,i)=>sum+Number(i.price||0)*i.qty,0),[cart]);
  const change=e=>setForm({...form,[e.target.name]:e.target.value});
  const placeOrder=async e=>{
    e.preventDefault();setBusy(true);setError('');
    const res=await fetch('/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({...form,items:cart.map(i=>({bookId:i.id,quantity:i.qty}))})});
    const body=await res.json().catch(()=>({}));
    if(res.ok){localStorage.removeItem('book-store-cart');setCart([]);setOrder(body)}else setError(body.message||'Could not place the order.');
    setBusy(false);
  };

  if(order)return <main className="checkoutPage"><div className="orderSuccess"><div className="successIcon">✓</div><span className="eyebrow">ORDER CONFIRMED</span><h1>Thank you, {order.customerName}.</h1><p>Your order <strong>#{order.id}</strong> has been stored successfully.</p><div className="successMeta"><span>Status <strong>{order.status}</strong></span><span>Total <strong>{money(order.totalAmount)}</strong></span><span>Payment <strong>{order.paymentMethod}</strong></span></div><p className="muted">This is a demo order. No real payment has been charged.</p><a className="primaryLink" href="/">Continue shopping</a></div></main>;

  return <main className="checkoutPage">
    <header className="checkoutNav"><a className="brand" href="/">BOOKSTORE</a><span>Secure demo checkout</span></header>
    <div className="checkoutLayout">
      <section className="checkoutFormCard"><span className="eyebrow">CHECKOUT</span><h1>Shipping details</h1>
        {cart.length===0?<div className="emptyState"><p>Your cart is empty.</p><a href="/">Return to catalog</a></div>:<form onSubmit={placeOrder}>
          <div className="fieldGrid"><label>Full name<input name="customerName" value={form.customerName} onChange={change} required/></label><label>Email<input name="email" type="email" value={form.email} onChange={change} required/></label></div>
          <label>Phone<input name="phone" value={form.phone} onChange={change} required/></label>
          <label>Shipping address<textarea name="shippingAddress" rows="4" value={form.shippingAddress} onChange={change} required/></label>
          <div className="paymentBox"><h3>Payment</h3><label className="radioRow"><input type="radio" name="paymentMethod" value="COD" checked={form.paymentMethod==='COD'} onChange={change}/><span><strong>Cash on Delivery</strong><small>Enabled for this demo</small></span></label><label className="radioRow disabled"><input type="radio" disabled/><span><strong>Card / online payment</strong><small>Not connected to a real payment gateway yet</small></span></label></div>
          {error&&<div className="checkoutError">{error}</div>}
          <button className="placeOrder" disabled={busy}>{busy?'Placing order...':'Place order'}</button>
        </form>}
      </section>

      <aside className="orderSummary"><span className="eyebrow">YOUR ORDER</span><h2>{cart.reduce((n,i)=>n+i.qty,0)} items</h2>
        <div className="summaryItems">{cart.map(i=><div className="summaryItem" key={i.id}>{i.bookCover&&<img src={i.bookCover} alt=""/>}<div><strong>{i.title}</strong><small>Qty {i.qty}</small></div><span>{money(Number(i.price||0)*i.qty)}</span></div>)}</div>
        <div className="summaryLine"><span>Subtotal</span><strong>{money(total)}</strong></div><div className="summaryLine"><span>Shipping</span><strong>Free</strong></div><div className="summaryTotal"><span>Total</span><strong>{money(total)}</strong></div>
        <p className="summaryNote">The server recalculates this total from current book prices and checks stock before saving the order.</p>
      </aside>
    </div>
  </main>
}
