import React,{useEffect,useState}from'react';
import Brand from './Brand';

const readCart=()=>{try{return JSON.parse(localStorage.getItem('book-store-cart')||'[]')}catch{return[]}};
const money=v=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(v||0));

export default function Checkout(){
  const[cart,setCart]=useState(readCart),[user,setUser]=useState(undefined),[form,setForm]=useState({phone:'',shippingAddress:''}),[busy,setBusy]=useState(false),[error,setError]=useState(''),[order,setOrder]=useState(null),[mobileMenuOpen,setMobileMenuOpen]=useState(false);

  useEffect(()=>{fetch('/api/auth/me').then(r=>r.ok?r.json():null).then(setUser).catch(()=>setUser(null))},[]);
  const change=e=>setForm({...form,[e.target.name]:e.target.value});

  const placeOrder=async e=>{
    e.preventDefault();setBusy(true);setError('');
    const res=await fetch('/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({...form,items:cart.map(i=>({bookId:i.id,quantity:i.qty}))})});
    const body=await res.json().catch(()=>({}));
    if(res.ok){localStorage.removeItem('book-store-cart');setCart([]);setOrder(body)}
    else if(res.status===401){setUser(null);setError('Your session expired. Please sign in again.')}
    else setError(body.message||'Could not complete payment.');
    setBusy(false);
  };

  if(user===undefined)return <div className="pageState">Checking account...</div>;

  if(!user)return <main className="checkoutPage">
    <header className="checkoutNav"><Brand/><span className="checkoutDesktopStatus">Account required</span><button className="mobileMenuButton" aria-label="Open menu" onClick={()=>setMobileMenuOpen(true)}><span></span><span></span><span></span></button></header>
    {mobileMenuOpen&&<div className="mobileMenuOverlay" onMouseDown={e=>e.target===e.currentTarget&&setMobileMenuOpen(false)}><aside className="mobileMenuPanel"><div className="mobileMenuHead"><Brand/><button className="mobileMenuClose" onClick={()=>setMobileMenuOpen(false)}>×</button></div><a href="/">Store</a><a href="/login?next=%2Fcheckout">Login</a><a href="/register?next=%2Fcheckout">Register</a></aside></div>}
    <div className="checkoutAuthGate"><span className="eyebrow">CHECKOUT LOCKED</span><h1>Sign in before payment</h1><p>You can browse and build a cart as a guest, but an account is required to place an order.</p><div><a className="primaryLink" href="/login?next=%2Fcheckout">Sign in</a><a className="secondaryLink" href="/register?next=%2Fcheckout">Create account</a></div></div>
  </main>;

  if(order)return <main className="checkoutPage"><div className="orderSuccess"><div className="successIcon">✓</div><span className="eyebrow">PAYMENT COMPLETE</span><h1>Thank you, {order.customerName}.</h1><p>Order <strong>#{order.id}</strong> has been paid and stored successfully.</p><div className="successMeta"><span>Status <strong>{order.status}</strong></span><span>Total <strong>{money(0)}</strong></span><span>Payment <strong>{order.paymentMethod}</strong></span></div><p className="muted">Because the catalog price is 0đ, no external payment gateway or charge is required.</p><a className="primaryLink" href="/">Continue shopping</a></div></main>;

  return <main className="checkoutPage">
    <header className="checkoutNav"><Brand/><span className="checkoutDesktopStatus">Signed in as {user.email}</span><button className="mobileMenuButton" aria-label="Open menu" onClick={()=>setMobileMenuOpen(true)}><span></span><span></span><span></span></button></header>
    {mobileMenuOpen&&<div className="mobileMenuOverlay" onMouseDown={e=>e.target===e.currentTarget&&setMobileMenuOpen(false)}><aside className="mobileMenuPanel"><div className="mobileMenuHead"><Brand/><button className="mobileMenuClose" onClick={()=>setMobileMenuOpen(false)}>×</button></div><div className="mobileUserBlock"><small>Signed in as</small><strong>{user.fullName}</strong><span>{user.email}</span></div><a href="/">Store</a><button onClick={()=>setMobileMenuOpen(false)}>Checkout</button></aside></div>}
    <div className="checkoutLayout">
      <section className="checkoutFormCard"><span className="eyebrow">FREE CHECKOUT</span><h1>Shipping details</h1>
        {cart.length===0?<div className="emptyState"><p>Your cart is empty.</p><a href="/">Return to catalog</a></div>:<form onSubmit={placeOrder}>
          <div className="accountSummary"><span>Account</span><strong>{user.fullName}</strong><small>{user.email}</small></div>
          <label>Phone<input name="phone" value={form.phone} onChange={change} required/></label>
          <label>Shipping address<textarea name="shippingAddress" rows="4" value={form.shippingAddress} onChange={change} required/></label>
          <div className="paymentBox"><h3>Payment</h3><div className="freePayment"><span className="freeBadge">0đ</span><div><strong>Free payment</strong><small>Total amount is zero. Completing checkout immediately marks this order as PAID.</small></div></div></div>
          {error&&<div className="checkoutError">{error}</div>}
          <button className="placeOrder" disabled={busy}>{busy?'Processing...':'Pay 0đ & place order'}</button>
        </form>}
      </section>

      <aside className="orderSummary"><span className="eyebrow">YOUR ORDER</span><h2>{cart.reduce((n,i)=>n+i.qty,0)} items</h2>
        <div className="summaryItems">{cart.map(i=><div className="summaryItem" key={i.id}>{i.bookCover&&<img src={i.bookCover} alt=""/>}<div><strong>{i.title}</strong><small>Qty {i.qty}</small></div><span>{money(0)}</span></div>)}</div>
        <div className="summaryLine"><span>Subtotal</span><strong>{money(0)}</strong></div><div className="summaryLine"><span>Shipping</span><strong>{money(0)}</strong></div><div className="summaryTotal"><span>Total</span><strong>{money(0)}</strong></div>
        <p className="summaryNote">The backend still validates stock and the logged-in account before saving the paid order.</p>
      </aside>
    </div>
  </main>
}
