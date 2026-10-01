import React,{useEffect,useMemo,useState}from'react';

const read=(key,fallback)=>{try{return JSON.parse(localStorage.getItem(key)||JSON.stringify(fallback))}catch{return fallback}};
const money=v=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(v||0));

export default function Store(){
  const[books,setBooks]=useState([]),[categories,setCategories]=useState([]),[selectedCategory,setSelectedCategory]=useState('All');
  const[q,setQ]=useState(''),[loading,setLoading]=useState(true),[user,setUser]=useState(null);
  const[cartOpen,setCartOpen]=useState(false),[cart,setCart]=useState(()=>read('book-store-cart',[]));
  const[wishlist,setWishlist]=useState(()=>read('book-store-wishlist',[])),[wishlistOnly,setWishlistOnly]=useState(false),[notice,setNotice]=useState('');

  useEffect(()=>{
    fetch('/api/books').then(r=>r.json()).then(setBooks).catch(()=>setBooks([])).finally(()=>setLoading(false));
    fetch('/api/categories').then(r=>r.json()).then(setCategories).catch(()=>setCategories([]));
    fetch('/api/auth/me').then(r=>r.ok?r.json():null).then(setUser).catch(()=>setUser(null));
  },[]);
  useEffect(()=>localStorage.setItem('book-store-cart',JSON.stringify(cart)),[cart]);
  useEffect(()=>localStorage.setItem('book-store-wishlist',JSON.stringify(wishlist)),[wishlist]);

  const filtered=useMemo(()=>{
    const s=q.trim().toLowerCase();
    return books.filter(b=>
      (!wishlistOnly||wishlist.includes(b.id))&&
      (selectedCategory==='All'||b.category===selectedCategory)&&
      (!s||`${b.title} ${b.author} ${b.category}`.toLowerCase().includes(s))
    );
  },[books,q,wishlistOnly,wishlist,selectedCategory]);

  const cartCount=cart.reduce((n,i)=>n+i.qty,0);
  const flash=msg=>{setNotice(msg);setTimeout(()=>setNotice(''),1700)};
  const addToCart=book=>{setCart(c=>{const found=c.find(i=>i.id===book.id);return found?c.map(i=>i.id===book.id?{...i,price:0,qty:Math.min(i.qty+1,book.stock||99)}:i):[...c,{...book,price:0,qty:1}]});flash(`${book.title} added to cart`)};
  const toggleWishlist=book=>setWishlist(w=>w.includes(book.id)?w.filter(id=>id!==book.id):[...w,book.id]);
  const changeQty=(id,delta)=>setCart(c=>c.map(i=>i.id===id?{...i,qty:Math.max(0,Math.min(i.qty+delta,i.stock||99))}:i).filter(i=>i.qty>0));
  const logout=async()=>{await fetch('/api/auth/logout',{method:'POST'});setUser(null)};

  return <main>
    <header className="hero">
      <nav><strong>BOOKSTORE</strong><div className="navActions">
        <button className={"navPill "+(wishlistOnly?'active':'')} onClick={()=>setWishlistOnly(v=>!v)}>♡ Wishlist <span>{wishlist.length}</span></button>
        <a href="/admin">Admin</a>
        {user?<><span className="userPill">Hi, {user.fullName}</span><button className="navPill" onClick={logout}>Logout</button></>:<><a href="/login">Login</a><a href="/register">Register</a></>}
        <button className="cartButton" onClick={()=>setCartOpen(true)}>Cart <span>{cartCount}</span></button>
      </div></nav>
      <div className="heroText"><span className="eyebrow">{wishlistOnly?'YOUR SAVED BOOKS':'DISCOVER YOUR NEXT READ'}</span><h1>{wishlistOnly?'Wishlist':'Books worth'}<br/>{wishlistOnly?'collection.':'getting lost in.'}</h1><p>Explore around 50 books across multiple genres. All books are currently free.</p><input value={q} onChange={e=>setQ(e.target.value)} placeholder="Search title, author or category..."/></div>
    </header>

    <section className="categoryBar">
      <div className="categoryInner"><button className={selectedCategory==='All'?'active':''} onClick={()=>setSelectedCategory('All')}>All <span>{books.length}</span></button>{categories.map(c=><button key={c.id} className={selectedCategory===c.name?'active':''} onClick={()=>setSelectedCategory(c.name)}>{c.name} <span>{c.bookCount}</span></button>)}</div>
    </section>

    <section className="catalog">
      <div className="sectionTitle"><div><span className="eyebrow">{wishlistOnly?'WISHLIST':selectedCategory==='All'?'CATALOG':selectedCategory.toUpperCase()}</span><h2>{wishlistOnly?'Saved for later':selectedCategory==='All'?'Available books':selectedCategory}</h2></div><span>{filtered.length} titles</span></div>
      {loading?<p>Loading books...</p>:filtered.length===0?<div className="emptyState">No books found in this selection.</div>:<div className="grid">{filtered.map(b=><article className="card interactive" key={b.id}>
        <div className="cardMedia"><button className="coverButton" onClick={()=>window.location.href=`/books/${b.id}`} aria-label={`View ${b.title}`}><div className="cover">{b.bookCover?<img src={b.bookCover} alt={b.title}/>:<div className="placeholder">BOOK</div>}</div></button><button className={"heart "+(wishlist.includes(b.id)?'saved':'')} onClick={()=>toggleWishlist(b)} aria-label="Toggle wishlist">{wishlist.includes(b.id)?'♥':'♡'}</button></div>
        <div className="cardBody"><div className="cardMain"><span className="tag">{b.category}</span><h3 onClick={()=>window.location.href=`/books/${b.id}`}>{b.title}</h3><p className="author">{b.author}</p><p className="price">{money(0)}</p><p className="description">{b.description}</p></div><div className="cardFooter"><div className="stock">{b.stock>0?`${b.stock} in stock`:'Out of stock'}</div><div className="cardActions"><button className="ghost" onClick={()=>window.location.href=`/books/${b.id}`}>Details</button><button onClick={()=>addToCart(b)} disabled={b.stock<=0}>Add to cart</button></div></div></div>
      </article>)}</div>}
    </section>

    {cartOpen&&<div className="overlay cartOverlay" onMouseDown={e=>e.target===e.currentTarget&&setCartOpen(false)}><aside className="cartPanel"><div className="cartHeader"><div><span className="eyebrow">YOUR CART</span><h2>{cartCount} item{cartCount===1?'':'s'}</h2></div><button className="close" onClick={()=>setCartOpen(false)}>×</button></div>{!cart.length?<div className="emptyCart">Your cart is empty.<br/><button onClick={()=>setCartOpen(false)}>Browse books</button></div>:<><div className="cartItems">{cart.map(i=><div className="cartItem" key={i.id}>{i.bookCover&&<img src={i.bookCover} alt=""/>}<div className="cartInfo"><strong>{i.title}</strong><small>{money(0)}</small><div className="qty"><button onClick={()=>changeQty(i.id,-1)}>−</button><span>{i.qty}</span><button onClick={()=>changeQty(i.id,1)} disabled={i.qty>=i.stock}>+</button></div></div><button className="remove" onClick={()=>setCart(c=>c.filter(x=>x.id!==i.id))}>Remove</button></div>)}</div><div className="cartBottom"><p>{user?'Ready to complete your free order.':'You must sign in before payment.'}</p><button className="checkout" onClick={()=>window.location.href=user?'/checkout':'/login?next=%2Fcheckout'}>{user?'Checkout 0đ':'Sign in to checkout'}</button></div></>}</aside></div>}
    {notice&&<div className="toast">{notice}</div>}
  </main>
}
