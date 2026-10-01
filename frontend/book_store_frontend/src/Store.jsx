import React,{useEffect,useMemo,useState}from'react';

const readCart=()=>{try{return JSON.parse(localStorage.getItem('book-store-cart')||'[]')}catch{return[]}};

export default function Store(){
  const[books,setBooks]=useState([]),[q,setQ]=useState(''),[loading,setLoading]=useState(true);
  const[selected,setSelected]=useState(null),[cartOpen,setCartOpen]=useState(false),[cart,setCart]=useState(readCart),[notice,setNotice]=useState('');

  useEffect(()=>{fetch('/api/books').then(r=>r.json()).then(setBooks).catch(()=>setBooks([])).finally(()=>setLoading(false));},[]);
  useEffect(()=>{localStorage.setItem('book-store-cart',JSON.stringify(cart));},[cart]);

  const filtered=useMemo(()=>{const s=q.trim().toLowerCase();return!s?books:books.filter(b=>`${b.title} ${b.author} ${b.category}`.toLowerCase().includes(s));},[books,q]);
  const cartCount=cart.reduce((n,i)=>n+i.qty,0);

  const addToCart=book=>{
    setCart(c=>{const found=c.find(i=>i.id===book.id);return found?c.map(i=>i.id===book.id?{...i,qty:Math.min(i.qty+1,book.stock||99)}:i):[...c,{...book,qty:1}]});
    setNotice(`${book.title} added to cart`);
    setTimeout(()=>setNotice(''),1800);
  };
  const changeQty=(id,delta)=>setCart(c=>c.map(i=>i.id===id?{...i,qty:Math.max(0,Math.min(i.qty+delta,i.stock||99))}:i).filter(i=>i.qty>0));
  const checkout=()=>{if(!cart.length)return;alert('Demo checkout successful. No real payment was charged.');setCart([]);setCartOpen(false);};

  return <main>
    <header className="hero">
      <nav><strong>BOOKSTORE</strong><div className="navActions"><a href="/admin">Admin</a><button className="cartButton" onClick={()=>setCartOpen(true)}>Cart <span>{cartCount}</span></button></div></nav>
      <div className="heroText"><span className="eyebrow">DISCOVER YOUR NEXT READ</span><h1>Books worth<br/>getting lost in.</h1><p>A Spring Boot + React bookstore demo with book details and a working local cart.</p><input value={q} onChange={e=>setQ(e.target.value)} placeholder="Search title, author or category..."/></div>
    </header>

    <section className="catalog">
      <div className="sectionTitle"><div><span className="eyebrow">CATALOG</span><h2>Available books</h2></div><span>{filtered.length} titles</span></div>
      {loading?<p>Loading books...</p>:<div className="grid">{filtered.map(b=><article className="card interactive" key={b.id}>
        <button className="coverButton" onClick={()=>setSelected(b)} aria-label={`View ${b.title}`}>
          <div className="cover">{b.bookCover?<img src={b.bookCover} alt={b.title}/>:<div className="placeholder">BOOK</div>}</div>
        </button>
        <div className="cardBody"><span className="tag">{b.category}</span><h3>{b.title}</h3><p className="author">{b.author}</p><p className="description">{b.description}</p><div className="cardFooter"><div className="stock">{b.stock>0?`${b.stock} in stock`:'Out of stock'}</div><div className="cardActions"><button className="ghost" onClick={()=>setSelected(b)}>Details</button><button onClick={()=>addToCart(b)} disabled={b.stock<=0}>Add to cart</button></div></div></div>
      </article>)}</div>}
    </section>

    {selected&&<div className="overlay" onMouseDown={e=>e.target===e.currentTarget&&setSelected(null)}>
      <div className="modal">
        <button className="close" onClick={()=>setSelected(null)}>×</button>
        <div className="modalCover">{selected.bookCover?<img src={selected.bookCover} alt={selected.title}/>:<div className="placeholder">BOOK</div>}</div>
        <div className="modalBody"><span className="tag">{selected.category}</span><h2>{selected.title}</h2><p className="author">by {selected.author}</p><p>{selected.description}</p><p className="stock">{selected.stock>0?`${selected.stock} copies available`:'Out of stock'}</p><button onClick={()=>{addToCart(selected);setSelected(null)}} disabled={selected.stock<=0}>Add to cart</button></div>
      </div>
    </div>}

    {cartOpen&&<div className="overlay cartOverlay" onMouseDown={e=>e.target===e.currentTarget&&setCartOpen(false)}>
      <aside className="cartPanel"><div className="cartHeader"><div><span className="eyebrow">YOUR CART</span><h2>{cartCount} item{cartCount===1?'':'s'}</h2></div><button className="close" onClick={()=>setCartOpen(false)}>×</button></div>
        {!cart.length?<div className="emptyCart">Your cart is empty.<br/><button onClick={()=>setCartOpen(false)}>Browse books</button></div>:<>
          <div className="cartItems">{cart.map(i=><div className="cartItem" key={i.id}>{i.bookCover&&<img src={i.bookCover} alt=""/>}<div className="cartInfo"><strong>{i.title}</strong><small>{i.author}</small><div className="qty"><button onClick={()=>changeQty(i.id,-1)}>−</button><span>{i.qty}</span><button onClick={()=>changeQty(i.id,1)} disabled={i.qty>=i.stock}>+</button></div></div><button className="remove" onClick={()=>setCart(c=>c.filter(x=>x.id!==i.id))}>Remove</button></div>)}</div>
          <div className="cartBottom"><p>This demo does not charge real money.</p><button className="checkout" onClick={checkout}>Demo checkout</button></div>
        </>}
      </aside>
    </div>}

    {notice&&<div className="toast">{notice}</div>}
  </main>
}
