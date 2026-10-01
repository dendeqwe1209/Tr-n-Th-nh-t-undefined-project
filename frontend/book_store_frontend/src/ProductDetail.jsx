import React,{useEffect,useMemo,useState}from'react';

const read=(key,fallback)=>{try{return JSON.parse(localStorage.getItem(key)||JSON.stringify(fallback))}catch{return fallback}};
const money=v=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(v||0));
const Stars=({value=0})=><span className="stars" aria-label={`${value} out of 5 stars`}>{[1,2,3,4,5].map(n=><span key={n} className={n<=Math.round(value)?'on':''}>★</span>)}</span>;

export default function ProductDetail({bookId}){
  const[book,setBook]=useState(null),[reviewData,setReviewData]=useState({averageRating:0,reviewCount:0,reviews:[]}),[loading,setLoading]=useState(true);
  const[qty,setQty]=useState(1),[wishlist,setWishlist]=useState(()=>read('book-store-wishlist',[])),[cart,setCart]=useState(()=>read('book-store-cart',[]));
  const[form,setForm]=useState({reviewerName:'',rating:5,comment:''}),[submitting,setSubmitting]=useState(false),[message,setMessage]=useState('');

  const loadReviews=()=>fetch(`/api/books/${bookId}/reviews`).then(r=>r.json()).then(setReviewData);
  useEffect(()=>{Promise.all([fetch(`/api/books/${bookId}`).then(r=>{if(!r.ok)throw new Error();return r.json()}),fetch(`/api/books/${bookId}/reviews`).then(r=>r.json())]).then(([b,r])=>{setBook(b);setReviewData(r)}).catch(()=>setBook(null)).finally(()=>setLoading(false));},[bookId]);
  useEffect(()=>localStorage.setItem('book-store-cart',JSON.stringify(cart)),[cart]);
  useEffect(()=>localStorage.setItem('book-store-wishlist',JSON.stringify(wishlist)),[wishlist]);

  const saved=wishlist.includes(Number(bookId));
  const cartCount=useMemo(()=>cart.reduce((n,i)=>n+i.qty,0),[cart]);
  const addToCart=(goCheckout=false)=>{
    if(!book)return;
    setCart(c=>{const found=c.find(i=>i.id===book.id);return found?c.map(i=>i.id===book.id?{...i,qty:Math.min(i.qty+qty,book.stock||99)}:i):[...c,{...book,qty:Math.min(qty,book.stock||99)}]});
    setMessage(`${book.title} added to cart`);
    if(goCheckout)setTimeout(()=>window.location.href='/checkout',100);
  };
  const toggleWishlist=()=>setWishlist(w=>saved?w.filter(id=>id!==Number(bookId)):[...w,Number(bookId)]);
  const submitReview=async e=>{
    e.preventDefault();setSubmitting(true);setMessage('');
    const res=await fetch(`/api/books/${bookId}/reviews`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({...form,rating:Number(form.rating)})});
    const body=await res.json().catch(()=>({}));
    if(res.ok){setForm({reviewerName:'',rating:5,comment:''});setMessage('Thanks! Your review was published.');await loadReviews()}else setMessage(body.message||'Could not publish review.');
    setSubmitting(false);
  };

  if(loading)return <div className="pageState">Loading book...</div>;
  if(!book)return <div className="pageState"><h2>Book not found</h2><a href="/">← Back to store</a></div>;

  return <main className="detailPage">
    <header className="detailNav"><a className="brand" href="/">BOOKSTORE</a><div><button className="linkButton" onClick={toggleWishlist}>{saved?'♥ Saved':'♡ Wishlist'}</button><a className="cartLink" href="/checkout">Cart ({cartCount})</a></div></header>

    <section className="productHero">
      <div className="productCover">{book.bookCover?<img src={book.bookCover} alt={book.title}/>:<div className="placeholder">BOOK</div>}</div>
      <div className="productInfo">
        <div className="breadcrumb"><a href="/">Home</a><span>›</span><span>{book.category}</span></div>
        <span className="tag">{book.category}</span>
        <h1>{book.title}</h1>
        <p className="productAuthor">by {book.author}</p>
        <div className="ratingLine"><Stars value={reviewData.averageRating}/><strong>{reviewData.averageRating||'New'}</strong><a href="#reviews">{reviewData.reviewCount} review{reviewData.reviewCount===1?'':'s'}</a></div>
        <div className="detailPrice">{money(book.price)}</div>
        <p className="productDescription">{book.description}</p>
        <p className={"availability "+(book.stock>0?'in':'out')}>{book.stock>0?`In stock · ${book.stock} copies available`:'Out of stock'}</p>

        <div className="purchaseRow">
          <div className="qtyPicker"><button onClick={()=>setQty(q=>Math.max(1,q-1))}>−</button><span>{qty}</span><button onClick={()=>setQty(q=>Math.min(book.stock||1,q+1))}>+</button></div>
          <button className="primaryAction" disabled={book.stock<=0} onClick={()=>addToCart(false)}>Add to cart</button>
          <button className="buyNow" disabled={book.stock<=0} onClick={()=>addToCart(true)}>Buy now</button>
        </div>
        <button className={"wishlistAction "+(saved?'saved':'')} onClick={toggleWishlist}>{saved?'♥ Remove from wishlist':'♡ Add to wishlist'}</button>
        {message&&<p className="inlineMessage">{message}</p>}
        <div className="checkoutNote"><strong>Checkout note:</strong> stock and pricing are re-validated by the server when the order is placed. This demo currently enables Cash on Delivery only; no real card payment is processed.</div>
      </div>
    </section>

    <section className="reviewsSection" id="reviews">
      <div className="reviewsHeader"><div><span className="eyebrow">READER REVIEWS</span><h2>Ratings & comments</h2></div><div className="ratingSummary"><strong>{reviewData.averageRating||'—'}</strong><div><Stars value={reviewData.averageRating}/><span>{reviewData.reviewCount} verified submission{reviewData.reviewCount===1?'':'s'}</span></div></div></div>
      <div className="reviewGrid">
        <form className="reviewForm" onSubmit={submitReview}>
          <h3>Share your opinion</h3>
          <label>Your name<input value={form.reviewerName} onChange={e=>setForm({...form,reviewerName:e.target.value})} placeholder="Anonymous"/></label>
          <label>Rating<select value={form.rating} onChange={e=>setForm({...form,rating:e.target.value})}><option value="5">5 — Excellent</option><option value="4">4 — Very good</option><option value="3">3 — Good</option><option value="2">2 — Fair</option><option value="1">1 — Poor</option></select></label>
          <label>Comment<textarea rows="5" required minLength="3" value={form.comment} onChange={e=>setForm({...form,comment:e.target.value})} placeholder="What did you think about this book?"/></label>
          <button disabled={submitting}>{submitting?'Publishing...':'Publish review'}</button>
        </form>
        <div className="reviewList">{reviewData.reviews.length===0?<div className="emptyState">No reviews yet. Be the first reader to leave one.</div>:reviewData.reviews.map(r=><article className="reviewCard" key={r.id}><div className="reviewTop"><div><strong>{r.reviewerName}</strong><Stars value={r.rating}/></div><time>{r.createdAt?new Date(r.createdAt).toLocaleDateString('vi-VN'):''}</time></div><p>{r.comment}</p></article>)}</div>
      </div>
    </section>
  </main>
}
