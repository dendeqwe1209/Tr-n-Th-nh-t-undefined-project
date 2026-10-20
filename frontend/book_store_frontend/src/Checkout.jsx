import React,{useEffect,useMemo,useState}from'react';
import Brand from './Brand';

const readCart=()=>{try{return JSON.parse(localStorage.getItem('book-store-cart')||'[]')}catch{return[]}};
const money=v=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(v||0));

export default function Checkout(){
  const[cart,setCart]=useState(readCart),[user,setUser]=useState(undefined),[form,setForm]=useState({phone:'',shippingAddress:''});
  const[busy,setBusy]=useState(false),[error,setError]=useState(''),[order,setOrder]=useState(null),[mobileMenuOpen,setMobileMenuOpen]=useState(false);
  const[promotionCode,setPromotionCode]=useState(''),[quote,setQuote]=useState(null),[vouchers,setVouchers]=useState([]);
  const[paymentCode,setPaymentCode]=useState('4242 4242 4242 4242');

  const items=useMemo(()=>cart.map(i=>({bookId:i.id,quantity:i.qty})),[cart]);
  const change=e=>setForm({...form,[e.target.name]:e.target.value});

  const loadQuote=async(code='')=>{
    if(!cart.length)return;
    setError('');
    const res=await fetch('/api/orders/quote',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({items,promotionCode:code})});
    const body=await res.json().catch(()=>({}));
    if(res.ok){setQuote(body);setPromotionCode(body.promotionCode||code||'')}
    else{setQuote(null);setError(body.message||'Could not calculate checkout total.')}
  };

  useEffect(()=>{
    fetch('/api/auth/me').then(r=>r.ok?r.json():null).then(async u=>{
      setUser(u);
      if(u){
        fetch('/api/promotions/my').then(r=>r.ok?r.json():[]).then(setVouchers).catch(()=>setVouchers([]));
      }
    }).catch(()=>setUser(null));
  },[]);

  useEffect(()=>{if(user&&cart.length)loadQuote('')},[user]); // eslint-disable-line react-hooks/exhaustive-deps

  const applyPromotion=()=>loadQuote(promotionCode);
  const useVoucher=code=>{setPromotionCode(code);loadQuote(code)};

  const placeOrder=async e=>{
    e.preventDefault();setBusy(true);setError('');
    const res=await fetch('/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({
      ...form,items,promotionCode:quote?.promotionCode||'',paymentMethod:'DEMO_CARD',paymentCode
    })});
    const body=await res.json().catch(()=>({}));
    if(res.ok){
      localStorage.removeItem('book-store-cart');setCart([]);setOrder(body);
    }else if(res.status===401){
      setUser(null);setError('Your session expired. Please sign in again.');
    }else setError(body.message||'Could not complete payment.');
    setBusy(false);
  };

  if(user===undefined)return <div className="pageState">Checking account...</div>;

  if(!user)return <main className="checkoutPage">
    <header className="checkoutNav"><Brand/><span className="checkoutDesktopStatus">Account required</span><button className="mobileMenuButton" aria-label="Open menu" onClick={()=>setMobileMenuOpen(true)}><span></span><span></span><span></span></button></header>
    {mobileMenuOpen&&<div className="mobileMenuOverlay" onMouseDown={e=>e.target===e.currentTarget&&setMobileMenuOpen(false)}><aside className="mobileMenuPanel"><div className="mobileMenuHead"><Brand/><button className="mobileMenuClose" onClick={()=>setMobileMenuOpen(false)}>×</button></div><a href="/">Store</a><a href="/login?next=%2Fcheckout">Login</a><a href="/register?next=%2Fcheckout">Register</a></aside></div>}
    <div className="checkoutAuthGate"><span className="eyebrow">CHECKOUT LOCKED</span><h1>Sign in before payment</h1><p>You can browse and build a cart as a guest, but an account is required to place an order and use account vouchers.</p><div><a className="primaryLink" href="/login?next=%2Fcheckout">Sign in</a><a className="secondaryLink" href="/register?next=%2Fcheckout">Create account</a></div></div>
  </main>;

  if(order)return <main className="checkoutPage"><div className="orderSuccess"><div className="successIcon">✓</div><span className="eyebrow">DEMOPAY APPROVED</span><h1>Thank you, {order.customerName}.</h1><p>Order <strong>#{order.id}</strong> has been paid in the sandbox and stored successfully.</p><div className="successMeta"><span>Status <strong>{order.status}</strong></span><span>Total <strong>{money(order.totalAmount)}</strong></span><span>Payment <strong>{order.paymentMethod}</strong></span></div>{order.promotionCode&&<p className="muted">Promotion used: <strong>{order.promotionCode}</strong>. Account vouchers are consumed only after successful payment.</p>}<p className="muted">This is a simulated payment. No real card charge was made.</p><a className="primaryLink" href="/">Continue shopping</a></div></main>;

  const subtotal=quote?.subtotal??cart.reduce((sum,i)=>sum+Number(i.price||0)*i.qty,0);
  const discount=quote?.discount??0;
  const total=quote?.total??subtotal;

  return <main className="checkoutPage">
    <header className="checkoutNav"><Brand/><span className="checkoutDesktopStatus">Signed in as {user.email}</span><button className="mobileMenuButton" aria-label="Open menu" onClick={()=>setMobileMenuOpen(true)}><span></span><span></span><span></span></button></header>
    {mobileMenuOpen&&<div className="mobileMenuOverlay" onMouseDown={e=>e.target===e.currentTarget&&setMobileMenuOpen(false)}><aside className="mobileMenuPanel"><div className="mobileMenuHead"><Brand/><button className="mobileMenuClose" onClick={()=>setMobileMenuOpen(false)}>×</button></div><div className="mobileUserBlock"><small>Signed in as</small><strong>{user.fullName}</strong><span>{user.email}</span></div><a href="/">Store</a><button onClick={()=>setMobileMenuOpen(false)}>Checkout</button></aside></div>}

    <div className="checkoutLayout">
      <section className="checkoutFormCard"><span className="eyebrow">DEMOPAY SANDBOX</span><h1>Shipping & payment</h1>
        {cart.length===0?<div className="emptyState"><p>Your cart is empty.</p><a href="/">Return to catalog</a></div>:<form onSubmit={placeOrder}>
          <div className="accountSummary"><span>Account</span><strong>{user.fullName}</strong><small>{user.email}</small></div>
          <label>Phone<input name="phone" value={form.phone} onChange={change} required/></label>
          <label>Shipping address<textarea name="shippingAddress" rows="4" value={form.shippingAddress} onChange={change} required/></label>

          <div className="promotionBox">
            <h3>Voucher / discount code</h3>
            {vouchers.length>0&&<div className="voucherList">{vouchers.map(v=><button type="button" key={v.id} className={quote?.promotionCode===v.code?'selected':''} onClick={()=>useVoucher(v.code)}><strong>{v.name}</strong><span>{v.code}</span><small>{v.discountType==='PERCENT'?v.value+'% off':money(v.value)}{v.maxDiscount?' · max '+money(v.maxDiscount):''}{v.expiresAt?' · expires '+new Date(v.expiresAt).toLocaleDateString('vi-VN'):' · no expiry'}</small></button>)}</div>}
            <div className="promotionInput"><input value={promotionCode} onChange={e=>setPromotionCode(e.target.value.toUpperCase())} placeholder="Enter voucher or discount code"/><button type="button" onClick={applyPromotion}>Apply</button></div>
            {quote?.promotionName&&<div className="promoApplied">✓ {quote.promotionName} applied</div>}
          </div>

          <div className="paymentBox"><h3>Payment</h3>
            <div className="demoPayCard"><div className="demoPayLogo">DP</div><div><strong>DemoPay Sandbox</strong><small>Simulated gateway — no real charge.</small></div></div>
            <label>Sandbox card number<input value={paymentCode} onChange={e=>setPaymentCode(e.target.value)} inputMode="numeric" required/></label>
            <div className="sandboxHints"><span><strong>Success:</strong> 4242 4242 4242 4242</span><span><strong>Decline:</strong> 4000 0000 0000 0002</span></div>
          </div>

          {error&&<div className="checkoutError">{error}</div>}
          <button className="placeOrder" disabled={busy}>{busy?'Authorizing DemoPay...':'Pay '+money(total)+' with DemoPay'}</button>
        </form>}
      </section>

      <aside className="orderSummary"><span className="eyebrow">YOUR ORDER</span><h2>{cart.reduce((n,i)=>n+i.qty,0)} items</h2>
        <div className="summaryItems">{cart.map(i=><div className="summaryItem" key={i.id}>{i.bookCover&&<img src={i.bookCover} alt=""/>}<div><strong>{i.title}</strong><small>Qty {i.qty}</small></div><span>{money(Number(i.price||0)*i.qty)}</span></div>)}</div>
        <div className="summaryLine"><span>Subtotal</span><strong>{money(subtotal)}</strong></div>
        <div className="summaryLine discountLine"><span>Discount</span><strong>− {money(discount)}</strong></div>
        <div className="summaryLine"><span>Shipping</span><strong>{money(0)}</strong></div>
        <div className="summaryTotal"><span>Total</span><strong>{money(total)}</strong></div>
        <p className="summaryNote">The backend recalculates price, promotion and stock before DemoPay authorization. Client totals are never trusted.</p>
      </aside>
    </div>
  </main>
}
