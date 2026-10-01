import React,{useEffect,useMemo,useState}from'react';
import Brand from './Brand';

const emptyBook={title:'',author:'',category:'',stock:10,bookCover:'',description:'',previewText:'',featured:false};
const emptyStaff={fullName:'',email:'',phone:''};
const tabs=['overview','books','categories','orders','users','reviews','staff'];
const label=t=>t.charAt(0).toUpperCase()+t.slice(1);

export default function Admin(){
  const[tab,setTab]=useState('overview');
  const[overview,setOverview]=useState({});
  const[books,setBooks]=useState([]),[categories,setCategories]=useState([]),[orders,setOrders]=useState([]),[users,setUsers]=useState([]),[reviews,setReviews]=useState([]),[staff,setStaff]=useState([]);
  const[bookForm,setBookForm]=useState(emptyBook),[editingBook,setEditingBook]=useState(null),[bookSearch,setBookSearch]=useState('');
  const[categoryName,setCategoryName]=useState(''),[editingCategory,setEditingCategory]=useState(null);
  const[staffForm,setStaffForm]=useState(emptyStaff),[editingStaff,setEditingStaff]=useState(null);
  const[msg,setMsg]=useState('');

  const json=async(url,options)=>{const r=await fetch(url,options);const body=await r.json().catch(()=>({}));if(!r.ok)throw new Error(body.message||'Request failed');return body};
  const loadOverview=()=>json('/api/admin/overview').then(setOverview).catch(()=>{});
  const loadBooks=()=>json('/api/admin/books').then(setBooks).catch(()=>{});
  const loadCategories=()=>json('/api/admin/categories').then(setCategories).catch(()=>{});
  const loadOrders=()=>json('/api/admin/orders').then(setOrders).catch(()=>{});
  const loadUsers=()=>json('/api/admin/users').then(setUsers).catch(()=>{});
  const loadReviews=()=>json('/api/admin/reviews').then(setReviews).catch(()=>{});
  const loadStaff=()=>json('/admin/staff-management/staffs').then(setStaff).catch(()=>{});
  const refresh=()=>{loadOverview();loadBooks();loadCategories();loadOrders();loadUsers();loadReviews();loadStaff()};

  useEffect(()=>{refresh()},[]);
  const flash=m=>{setMsg(m);setTimeout(()=>setMsg(''),2500)};
  const bookChange=e=>setBookForm({...bookForm,[e.target.name]:e.target.type==='checkbox'?e.target.checked:e.target.value});
  const staffChange=e=>setStaffForm({...staffForm,[e.target.name]:e.target.value});

  const saveBook=async e=>{
    e.preventDefault();
    try{
      const payload={...bookForm,stock:Number(bookForm.stock)};
      await json(editingBook?`/api/admin/books/${editingBook}`:'/api/admin/books',{method:editingBook?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
      setBookForm(emptyBook);setEditingBook(null);flash('Book saved');refresh();
    }catch(err){flash(err.message)}
  };
  const editBook=b=>{setEditingBook(b.id);setBookForm({title:b.title,author:b.author,category:b.category,stock:b.stock,bookCover:b.bookCover||'',description:b.description||'',previewText:b.previewText||'',featured:!!b.featured});window.scrollTo({top:0,behavior:'smooth'})};
  const deleteBook=async id=>{if(!window.confirm('Delete this book?'))return;try{await fetch(`/api/admin/books/${id}`,{method:'DELETE'});flash('Book deleted');refresh()}catch{flash('Could not delete book')}};

  const saveCategory=async e=>{
    e.preventDefault();try{
      await json(editingCategory?`/api/admin/categories/${editingCategory}`:'/api/admin/categories',{method:editingCategory?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name:categoryName})});
      setCategoryName('');setEditingCategory(null);flash('Category saved');refresh();
    }catch(err){flash(err.message)}
  };
  const deleteCategory=async id=>{if(!window.confirm('Delete this category?'))return;const r=await fetch(`/api/admin/categories/${id}`,{method:'DELETE'});const b=await r.json().catch(()=>({}));if(!r.ok)flash(b.message||'Could not delete category');else{flash('Category deleted');refresh()}};

  const updateOrder=async(id,status)=>{try{await json(`/api/admin/orders/${id}/status`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({status})});flash('Order updated');loadOrders();loadOverview()}catch(err){flash(err.message)}};
  const deleteReview=async id=>{if(!window.confirm('Delete this review?'))return;await fetch(`/api/admin/reviews/${id}`,{method:'DELETE'});flash('Review deleted');loadReviews();loadOverview()};

  const saveStaff=async e=>{
    e.preventDefault();try{
      const url=editingStaff?`/admin/staff-management/staffs/${editingStaff}`:'/admin/staff-management';
      const r=await fetch(url,{method:editingStaff?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(staffForm)});
      if(!r.ok)throw new Error();
      setStaffForm(emptyStaff);setEditingStaff(null);flash('Staff saved');loadStaff();loadOverview();
    }catch{flash('Could not save staff')}
  };
  const deleteStaff=async id=>{if(!window.confirm('Delete this staff member?'))return;await fetch(`/admin/staff-management/staffs/${id}`,{method:'DELETE'});loadStaff();loadOverview()};
  const editStaff=s=>{setEditingStaff(s.id);setStaffForm({fullName:s.fullName||'',email:s.email||'',phone:s.phone||''})};

  const filteredBooks=useMemo(()=>{const q=bookSearch.trim().toLowerCase();return !q?books:books.filter(b=>`${b.title} ${b.author} ${b.category}`.toLowerCase().includes(q))},[books,bookSearch]);

  return <main className="adminShell">
    <aside className="adminSidebar">
      <Brand className="adminBrand"/>
      <p>Admin workspace</p>
      <nav>{tabs.map(t=><button key={t} className={tab===t?'active':''} onClick={()=>setTab(t)}>{label(t)}{t==='books'&&<span>{overview.books||0}</span>}{t==='orders'&&<span>{overview.orders||0}</span>}</button>)}</nav>
      <a className="backStore" href="/">← View storefront</a>
    </aside>

    <section className="adminContent">
      <header className="adminHeader"><div><span className="eyebrow">ADMIN PANEL</span><h1>{label(tab)}</h1></div>{msg&&<div className="adminToast">{msg}</div>}</header>

      {tab==='overview'&&<div>
        <div className="statGrid">
          {[
            ['Books',overview.books],['Categories',overview.categories],['Users',overview.users],
            ['Orders',overview.orders],['Reviews',overview.reviews],['Staff',overview.staff],['Stock',overview.stock]
          ].map(([k,v])=><article className="statCard" key={k}><span>{k}</span><strong>{v??'—'}</strong></article>)}
        </div>
        <section className="adminPanel"><h2>Store status</h2><p>The customer site currently uses account-based reviews and checkout, 0đ pricing, category browsing and persistent MySQL orders.</p><div className="quickLinks"><button onClick={()=>setTab('books')}>Manage books</button><button onClick={()=>setTab('orders')}>View orders</button><button onClick={()=>setTab('reviews')}>Moderate reviews</button></div></section>
      </div>}

      {tab==='books'&&<div className="adminTwoCol">
        <form className="adminPanel formPanel" onSubmit={saveBook}>
          <h2>{editingBook?'Edit book':'Add book'}</h2>
          <label>Title<input name="title" value={bookForm.title} onChange={bookChange} required/></label>
          <label>Author<input name="author" value={bookForm.author} onChange={bookChange} required/></label>
          <label>Category<select name="category" value={bookForm.category} onChange={bookChange} required><option value="">Select category</option>{categories.map(c=><option key={c.id} value={c.name}>{c.name}</option>)}</select></label>
          <label>Stock<input name="stock" type="number" min="0" value={bookForm.stock} onChange={bookChange} required/></label>
          <label>Cover URL<input name="bookCover" value={bookForm.bookCover} onChange={bookChange} placeholder="Optional"/></label>
          <label>Description<textarea name="description" rows="4" value={bookForm.description} onChange={bookChange}/></label><label>Preview text<textarea name="previewText" rows="7" value={bookForm.previewText} onChange={bookChange} placeholder="Short spoiler-light sample shown when the reader clicks Read sample"/></label><small className="adminFieldHint">Use an original preview/summary rather than copying copyrighted book text.</small><label className="featuredCheck"><input type="checkbox" name="featured" checked={bookForm.featured} onChange={bookChange}/><span>Show this book in the storefront slider</span></label>
          <div className="fixedPrice">Price <strong>0 ₫</strong></div>
          <button className="adminPrimary">{editingBook?'Update book':'Add book'}</button>
          {editingBook&&<button type="button" className="adminSecondary" onClick={()=>{setEditingBook(null);setBookForm(emptyBook)}}>Cancel</button>}
        </form>
        <section className="adminPanel">
          <div className="tableHeader"><div><h2>Catalog</h2><small>{books.length} books</small></div><input value={bookSearch} onChange={e=>setBookSearch(e.target.value)} placeholder="Search books..."/></div>
          <div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Book</th><th>Category</th><th>Featured</th><th>Stock</th><th>Price</th><th></th></tr></thead><tbody>{filteredBooks.map(b=><tr key={b.id}><td><div className="bookCell">{b.bookCover&&<img src={b.bookCover} alt=""/>}<div><strong>{b.title}</strong><small>{b.author}</small></div></div></td><td>{b.category}</td><td>{b.featured?'Yes':'—'}</td><td>{b.stock}</td><td>0 ₫</td><td className="rowActions"><button onClick={()=>editBook(b)}>Edit</button><button className="dangerText" onClick={()=>deleteBook(b.id)}>Delete</button></td></tr>)}</tbody></table></div>
        </section>
      </div>}

      {tab==='categories'&&<div className="adminTwoCol compactLeft">
        <form className="adminPanel formPanel" onSubmit={saveCategory}><h2>{editingCategory?'Rename category':'New category'}</h2><label>Name<input value={categoryName} onChange={e=>setCategoryName(e.target.value)} required/></label><button className="adminPrimary">{editingCategory?'Save name':'Add category'}</button>{editingCategory&&<button type="button" className="adminSecondary" onClick={()=>{setEditingCategory(null);setCategoryName('')}}>Cancel</button>}</form>
        <section className="adminPanel"><h2>Book categories</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Category</th><th>Books</th><th></th></tr></thead><tbody>{categories.map(c=><tr key={c.id}><td><strong>{c.name}</strong></td><td>{c.bookCount}</td><td className="rowActions"><button onClick={()=>{setEditingCategory(c.id);setCategoryName(c.name)}}>Rename</button><button className="dangerText" onClick={()=>deleteCategory(c.id)}>Delete</button></td></tr>)}</tbody></table></div></section>
      </div>}

      {tab==='orders'&&<section className="adminPanel"><h2>Customer orders</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Order</th><th>Customer</th><th>Items</th><th>Total</th><th>Status</th><th>Date</th></tr></thead><tbody>{orders.map(o=><tr key={o.id}><td>#{o.id}</td><td><strong>{o.customerName}</strong><small>{o.email}</small></td><td>{(o.items||[]).reduce((n,i)=>n+i.quantity,0)}</td><td>0 ₫</td><td><select value={o.status} onChange={e=>updateOrder(o.id,e.target.value)}>{['PAID','PROCESSING','SHIPPED','COMPLETED','CANCELLED'].map(s=><option key={s}>{s}</option>)}</select></td><td>{o.createdAt?new Date(o.createdAt).toLocaleString('vi-VN'):''}</td></tr>)}</tbody></table></div></section>}

      {tab==='users'&&<section className="adminPanel"><h2>Registered customers</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Registered</th></tr></thead><tbody>{users.map(u=><tr key={u.id}><td>#{u.id}</td><td><strong>{u.fullName}</strong></td><td>{u.email}</td><td>{u.createdAt?new Date(u.createdAt).toLocaleString('vi-VN'):''}</td></tr>)}</tbody></table></div></section>}

      {tab==='reviews'&&<section className="adminPanel"><h2>Ratings & comments</h2><div className="reviewAdminList">{reviews.map(r=><article key={r.id}><div><strong>{r.bookTitle}</strong><span>{'★'.repeat(r.rating)}{'☆'.repeat(5-r.rating)}</span><small>{r.reviewerName} · {r.createdAt?new Date(r.createdAt).toLocaleDateString('vi-VN'):''}</small></div><p>{r.comment}</p><button className="dangerText" onClick={()=>deleteReview(r.id)}>Delete review</button></article>)}</div></section>}

      {tab==='staff'&&<div className="adminTwoCol compactLeft">
        <form className="adminPanel formPanel" onSubmit={saveStaff}><h2>{editingStaff?'Edit staff':'Add staff'}</h2><label>Full name<input name="fullName" value={staffForm.fullName} onChange={staffChange} required/></label><label>Email<input name="email" type="email" value={staffForm.email} onChange={staffChange}/></label><label>Phone<input name="phone" value={staffForm.phone} onChange={staffChange}/></label><button className="adminPrimary">{editingStaff?'Update':'Add staff'}</button>{editingStaff&&<button type="button" className="adminSecondary" onClick={()=>{setEditingStaff(null);setStaffForm(emptyStaff)}}>Cancel</button>}</form>
        <section className="adminPanel"><h2>Staff members</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Name</th><th>Email</th><th>Phone</th><th></th></tr></thead><tbody>{staff.map(s=><tr key={s.id}><td>{s.fullName}</td><td>{s.email}</td><td>{s.phone}</td><td className="rowActions"><button onClick={()=>editStaff(s)}>Edit</button><button className="dangerText" onClick={()=>deleteStaff(s.id)}>Delete</button></td></tr>)}</tbody></table></div></section>
      </div>}
    </section>
  </main>
}
