import React,{useEffect,useMemo,useState}from'react';
import Brand from './Brand';

const money=v=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(v||0));
const emptyBook={title:'',author:'',category:'',stock:10,price:129000,bookCover:'',description:'',previewText:'',featured:false};
const emptyStaff={fullName:'',email:'',phone:''};
const emptyVoucher={userId:'',code:'',name:'',discountType:'PERCENT',value:20,maxDiscount:50000,expiresAt:''};
const emptyCode={code:'',name:'',discountType:'PERCENT',value:10,maxDiscount:'',expiresAt:'',usageLimit:''};
const tabs=['overview','books','categories','orders','users','promotions','reviews','staff'];
const label=t=>t.charAt(0).toUpperCase()+t.slice(1);

export default function Admin(){
  const[authUser,setAuthUser]=useState(undefined),[tab,setTab]=useState('overview'),[overview,setOverview]=useState({});
  const[books,setBooks]=useState([]),[categories,setCategories]=useState([]),[orders,setOrders]=useState([]),[users,setUsers]=useState([]),[reviews,setReviews]=useState([]),[staff,setStaff]=useState([]);
  const[vouchers,setVouchers]=useState([]),[codes,setCodes]=useState([]);
  const[bookForm,setBookForm]=useState(emptyBook),[editingBook,setEditingBook]=useState(null),[bookSearch,setBookSearch]=useState('');
  const[categoryName,setCategoryName]=useState(''),[editingCategory,setEditingCategory]=useState(null);
  const[staffForm,setStaffForm]=useState(emptyStaff),[editingStaff,setEditingStaff]=useState(null);
  const[voucherForm,setVoucherForm]=useState(emptyVoucher),[codeForm,setCodeForm]=useState(emptyCode);
  const[msg,setMsg]=useState(''),[adminMenuOpen,setAdminMenuOpen]=useState(false);

  const json=async(url,options)=>{const r=await fetch(url,options);const body=await r.json().catch(()=>({}));if(!r.ok)throw new Error(body.message||'Request failed');return body};
  const loadOverview=()=>json('/api/admin/overview').then(setOverview);
  const loadBooks=()=>json('/api/admin/books').then(setBooks);
  const loadCategories=()=>json('/api/admin/categories').then(setCategories);
  const loadOrders=()=>json('/api/admin/orders').then(setOrders);
  const loadUsers=()=>json('/api/admin/users').then(setUsers);
  const loadReviews=()=>json('/api/admin/reviews').then(setReviews);
  const loadStaff=()=>json('/admin/staff-management/staffs').then(setStaff);
  const loadPromotions=()=>Promise.all([json('/api/admin/promotions/vouchers'),json('/api/admin/promotions/codes')]).then(([v,c])=>{setVouchers(v);setCodes(c)});
  const refresh=()=>Promise.allSettled([loadOverview(),loadBooks(),loadCategories(),loadOrders(),loadUsers(),loadReviews(),loadStaff(),loadPromotions()]);

  useEffect(()=>{
    fetch('/api/auth/me').then(r=>r.ok?r.json():null).then(u=>{setAuthUser(u);if(u?.role==='ADMIN')refresh()}).catch(()=>setAuthUser(null));
  },[]);

  const flash=m=>{setMsg(m);setTimeout(()=>setMsg(''),2600)};
  const bookChange=e=>setBookForm({...bookForm,[e.target.name]:e.target.type==='checkbox'?e.target.checked:e.target.value});
  const staffChange=e=>setStaffForm({...staffForm,[e.target.name]:e.target.value});

  const saveBook=async e=>{e.preventDefault();try{
    const payload={...bookForm,stock:Number(bookForm.stock),price:Number(bookForm.price)};
    await json(editingBook?'/api/admin/books/'+editingBook:'/api/admin/books',{method:editingBook?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
    setBookForm(emptyBook);setEditingBook(null);flash('Book saved');refresh();
  }catch(err){flash(err.message)}};
  const editBook=b=>{setEditingBook(b.id);setBookForm({title:b.title,author:b.author,category:b.category,stock:b.stock,price:b.price,bookCover:b.bookCover||'',description:b.description||'',previewText:b.previewText||'',featured:!!b.featured});window.scrollTo({top:0,behavior:'smooth'})};
  const deleteBook=async id=>{if(!window.confirm('Delete this book?'))return;const r=await fetch('/api/admin/books/'+id,{method:'DELETE'});if(r.ok){flash('Book deleted');refresh()}else flash('Could not delete book')};

  const saveCategory=async e=>{e.preventDefault();try{await json(editingCategory?'/api/admin/categories/'+editingCategory:'/api/admin/categories',{method:editingCategory?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name:categoryName})});setCategoryName('');setEditingCategory(null);flash('Category saved');refresh()}catch(err){flash(err.message)}};
  const deleteCategory=async id=>{if(!window.confirm('Delete this category?'))return;try{await json('/api/admin/categories/'+id,{method:'DELETE'});flash('Category deleted');refresh()}catch(err){flash(err.message)}};
  const updateOrder=async(id,status)=>{try{await json('/api/admin/orders/'+id+'/status',{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({status})});flash('Order updated');loadOrders()}catch(err){flash(err.message)}};
  const updateRole=async(id,role)=>{try{await json('/api/admin/users/'+id+'/role',{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({role})});flash('Account role updated');loadUsers()}catch(err){flash(err.message)}};
  const deleteReview=async id=>{if(!window.confirm('Delete this review?'))return;await fetch('/api/admin/reviews/'+id,{method:'DELETE'});flash('Review deleted');loadReviews();loadOverview()};

  const saveStaff=async e=>{e.preventDefault();try{const url=editingStaff?'/admin/staff-management/staffs/'+editingStaff:'/admin/staff-management';const r=await fetch(url,{method:editingStaff?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(staffForm)});if(!r.ok)throw new Error();setStaffForm(emptyStaff);setEditingStaff(null);flash('Staff saved');loadStaff()}catch{flash('Could not save staff')}};
  const deleteStaff=async id=>{if(!window.confirm('Delete this staff member?'))return;await fetch('/admin/staff-management/staffs/'+id,{method:'DELETE'});loadStaff()};
  const editStaff=s=>{setEditingStaff(s.id);setStaffForm({fullName:s.fullName||'',email:s.email||'',phone:s.phone||''})};

  const saveVoucher=async e=>{e.preventDefault();try{
    const payload={...voucherForm,userId:Number(voucherForm.userId),value:Number(voucherForm.value),maxDiscount:voucherForm.maxDiscount===''?null:Number(voucherForm.maxDiscount),expiresAt:voucherForm.expiresAt||null};
    await json('/api/admin/promotions/vouchers',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
    setVoucherForm(emptyVoucher);flash('Voucher created');loadPromotions();loadOverview();
  }catch(err){flash(err.message)}};

  const saveCode=async e=>{e.preventDefault();try{
    const payload={...codeForm,value:Number(codeForm.value),maxDiscount:codeForm.maxDiscount===''?null:Number(codeForm.maxDiscount),expiresAt:codeForm.expiresAt||null,usageLimit:codeForm.usageLimit===''?null:Number(codeForm.usageLimit)};
    await json('/api/admin/promotions/codes',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
    setCodeForm(emptyCode);flash('Discount code created');loadPromotions();loadOverview();
  }catch(err){flash(err.message)}};

  const toggleVoucher=async v=>{try{await json('/api/admin/promotions/vouchers/'+v.id,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({name:v.name,discountType:v.discountType,value:v.value,maxDiscount:v.maxDiscount,expiresAt:v.expiresAt,active:!v.active})});loadPromotions()}catch(err){flash(err.message)}};
  const toggleCode=async c=>{try{await json('/api/admin/promotions/codes/'+c.id,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({name:c.name,discountType:c.discountType,value:c.value,maxDiscount:c.maxDiscount,expiresAt:c.expiresAt,usageLimit:c.usageLimit,active:!c.active})});loadPromotions()}catch(err){flash(err.message)}};

  const logout=async()=>{await fetch('/api/auth/logout',{method:'POST'});window.location.href='/login?next=%2Fadmin'};
  const filteredBooks=useMemo(()=>{const q=bookSearch.trim().toLowerCase();return !q?books:books.filter(b=>(b.title+' '+b.author+' '+b.category).toLowerCase().includes(q))},[books,bookSearch]);

  if(authUser===undefined)return <div className="pageState">Checking administrator access...</div>;
  if(!authUser||authUser.role!=='ADMIN')return <main className="adminAccessDenied"><Brand/><div><span className="eyebrow">ADMIN ONLY</span><h1>Administrator access required</h1><p>Only accounts with the ADMIN role can open this workspace or modify catalog data.</p><a className="primaryLink" href="/login?next=%2Fadmin">Admin login</a><a className="secondaryLink" href="/">Back to store</a></div></main>;

  return <main className="adminShell">
    <header className="adminMobileBar"><Brand className="adminBrand"/><button className="mobileMenuButton" aria-label="Open admin menu" onClick={()=>setAdminMenuOpen(true)}><span></span><span></span><span></span></button></header>
    {adminMenuOpen&&<div className="mobileMenuOverlay" onMouseDown={e=>e.target===e.currentTarget&&setAdminMenuOpen(false)}><aside className="mobileMenuPanel adminMobileMenu"><div className="mobileMenuHead"><Brand/><button className="mobileMenuClose" onClick={()=>setAdminMenuOpen(false)}>×</button></div><small className="mobileMenuLabel">ADMIN</small>{tabs.map(t=><button key={t} className={tab===t?'active':''} onClick={()=>{setTab(t);setAdminMenuOpen(false)}}>{label(t)}</button>)}<a href="/">← View storefront</a><button onClick={logout}>Logout</button></aside></div>}

    <aside className="adminSidebar"><Brand className="adminBrand"/><p>Admin workspace</p><div className="adminIdentity"><small>Signed in</small><strong>{authUser.username||authUser.email}</strong></div><nav>{tabs.map(t=><button key={t} className={tab===t?'active':''} onClick={()=>setTab(t)}>{label(t)}{t==='books'&&<span>{overview.books||0}</span>}{t==='orders'&&<span>{overview.orders||0}</span>}</button>)}</nav><a className="backStore" href="/">← View storefront</a><button className="adminLogout" onClick={logout}>Logout</button></aside>

    <section className="adminContent">
      <header className="adminHeader"><div><span className="eyebrow">ADMIN PANEL</span><h1>{label(tab)}</h1></div>{msg&&<div className="adminToast">{msg}</div>}</header>

      {tab==='overview'&&<div><div className="statGrid">{[['Books',overview.books],['Categories',overview.categories],['Users',overview.users],['Orders',overview.orders],['Reviews',overview.reviews],['Vouchers',overview.vouchers],['Discount codes',overview.discountCodes],['Stock',overview.stock]].map(([k,v])=><article className="statCard" key={k}><span>{k}</span><strong>{v??'—'}</strong></article>)}</div><section className="adminPanel"><h2>Store status</h2><p>Catalog pricing, DemoPay Sandbox, account roles, voucher/discount management and customer support are active.</p><div className="quickLinks"><button onClick={()=>setTab('books')}>Manage books</button><button onClick={()=>setTab('users')}>Account roles</button><button onClick={()=>setTab('promotions')}>Promotions</button></div></section></div>}

      {tab==='books'&&<div className="adminTwoCol"><form className="adminPanel formPanel" onSubmit={saveBook}><h2>{editingBook?'Edit book':'Add book'}</h2><label>Title<input name="title" value={bookForm.title} onChange={bookChange} required/></label><label>Author<input name="author" value={bookForm.author} onChange={bookChange} required/></label><label>Category<select name="category" value={bookForm.category} onChange={bookChange} required><option value="">Select category</option>{categories.map(c=><option key={c.id} value={c.name}>{c.name}</option>)}</select></label><label>Price (VND)<input name="price" type="number" min="1000" step="1000" value={bookForm.price} onChange={bookChange} required/></label><label>Stock<input name="stock" type="number" min="0" value={bookForm.stock} onChange={bookChange} required/></label><label>Cover URL<input name="bookCover" value={bookForm.bookCover} onChange={bookChange} placeholder="Optional"/></label><label>Description<textarea name="description" rows="4" value={bookForm.description} onChange={bookChange}/></label><label>Preview text<textarea name="previewText" rows="6" value={bookForm.previewText} onChange={bookChange}/></label><label className="featuredCheck"><input type="checkbox" name="featured" checked={bookForm.featured} onChange={bookChange}/><span>Show in storefront slider</span></label><button className="adminPrimary">{editingBook?'Update book':'Add book'}</button>{editingBook&&<button type="button" className="adminSecondary" onClick={()=>{setEditingBook(null);setBookForm(emptyBook)}}>Cancel</button>}</form><section className="adminPanel"><div className="tableHeader"><div><h2>Catalog</h2><small>{books.length} books</small></div><input value={bookSearch} onChange={e=>setBookSearch(e.target.value)} placeholder="Search books..."/></div><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Book</th><th>Category</th><th>Price</th><th>Featured</th><th>Stock</th><th></th></tr></thead><tbody>{filteredBooks.map(b=><tr key={b.id}><td><div className="bookCell">{b.bookCover&&<img src={b.bookCover} alt=""/>}<div><strong>{b.title}</strong><small>{b.author}</small></div></div></td><td>{b.category}</td><td>{money(b.price)}</td><td>{b.featured?'Yes':'—'}</td><td>{b.stock}</td><td className="rowActions"><button onClick={()=>editBook(b)}>Edit</button><button className="dangerText" onClick={()=>deleteBook(b.id)}>Delete</button></td></tr>)}</tbody></table></div></section></div>}

      {tab==='categories'&&<div className="adminTwoCol compactLeft"><form className="adminPanel formPanel" onSubmit={saveCategory}><h2>{editingCategory?'Rename category':'New category'}</h2><label>Name<input value={categoryName} onChange={e=>setCategoryName(e.target.value)} required/></label><button className="adminPrimary">{editingCategory?'Save name':'Add category'}</button>{editingCategory&&<button type="button" className="adminSecondary" onClick={()=>{setEditingCategory(null);setCategoryName('')}}>Cancel</button>}</form><section className="adminPanel"><h2>Book categories</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Category</th><th>Books</th><th></th></tr></thead><tbody>{categories.map(c=><tr key={c.id}><td><strong>{c.name}</strong></td><td>{c.bookCount}</td><td className="rowActions"><button onClick={()=>{setEditingCategory(c.id);setCategoryName(c.name)}}>Rename</button><button className="dangerText" onClick={()=>deleteCategory(c.id)}>Delete</button></td></tr>)}</tbody></table></div></section></div>}

      {tab==='orders'&&<section className="adminPanel"><h2>Customer orders</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Order</th><th>Customer</th><th>Items</th><th>Total</th><th>Promotion</th><th>Status</th><th>Date</th></tr></thead><tbody>{orders.map(o=><tr key={o.id}><td>#{o.id}</td><td><strong>{o.customerName}</strong><small>{o.email}</small></td><td>{(o.items||[]).reduce((n,i)=>n+i.quantity,0)}</td><td>{money(o.totalAmount)}</td><td>{o.promotionCode||'—'}</td><td><select value={o.status} onChange={e=>updateOrder(o.id,e.target.value)}>{['PAID','PROCESSING','SHIPPED','COMPLETED','CANCELLED'].map(s=><option key={s}>{s}</option>)}</select></td><td>{o.createdAt?new Date(o.createdAt).toLocaleString('vi-VN'):''}</td></tr>)}</tbody></table></div></section>}

      {tab==='users'&&<section className="adminPanel"><h2>Accounts & permissions</h2><p className="adminLead">Promote trusted accounts to ADMIN or return them to USER. Your own administrator role cannot be removed from this screen.</p><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>ID</th><th>Name</th><th>Login</th><th>Role</th><th>Registered</th></tr></thead><tbody>{users.map(u=><tr key={u.id}><td>#{u.id}</td><td><strong>{u.fullName}</strong></td><td>{u.username||u.email}<small>{u.username?u.email:''}</small></td><td><select value={u.role} disabled={u.id===authUser.id} onChange={e=>updateRole(u.id,e.target.value)}><option>USER</option><option>ADMIN</option></select></td><td>{u.createdAt?new Date(u.createdAt).toLocaleString('vi-VN'):''}</td></tr>)}</tbody></table></div></section>}

      {tab==='promotions'&&<div className="promotionAdmin">
        <div className="promotionForms">
          <form className="adminPanel formPanel" onSubmit={saveVoucher}><h2>Create account voucher</h2><label>Account<select value={voucherForm.userId} onChange={e=>setVoucherForm({...voucherForm,userId:e.target.value})} required><option value="">Select account</option>{users.map(u=><option key={u.id} value={u.id}>{u.fullName} — {u.email}</option>)}</select></label><label>Code (optional)<input value={voucherForm.code} onChange={e=>setVoucherForm({...voucherForm,code:e.target.value.toUpperCase()})}/></label><label>Name<input value={voucherForm.name} onChange={e=>setVoucherForm({...voucherForm,name:e.target.value})} placeholder="Birthday voucher"/></label><div className="promoFieldGrid"><label>Type<select value={voucherForm.discountType} onChange={e=>setVoucherForm({...voucherForm,discountType:e.target.value})}><option value="PERCENT">Percent</option><option value="FIXED">Fixed VND</option></select></label><label>Value<input type="number" min="1" value={voucherForm.value} onChange={e=>setVoucherForm({...voucherForm,value:e.target.value})}/></label></div><label>Max discount<input type="number" min="0" value={voucherForm.maxDiscount} onChange={e=>setVoucherForm({...voucherForm,maxDiscount:e.target.value})}/></label><label>Expires at (optional)<input type="datetime-local" value={voucherForm.expiresAt} onChange={e=>setVoucherForm({...voucherForm,expiresAt:e.target.value})}/></label><button className="adminPrimary">Create voucher</button></form>

          <form className="adminPanel formPanel" onSubmit={saveCode}><h2>Create discount code</h2><label>Code<input value={codeForm.code} onChange={e=>setCodeForm({...codeForm,code:e.target.value.toUpperCase()})} placeholder="WEEKEND10" required/></label><label>Name<input value={codeForm.name} onChange={e=>setCodeForm({...codeForm,name:e.target.value})} placeholder="Weekend campaign"/></label><div className="promoFieldGrid"><label>Type<select value={codeForm.discountType} onChange={e=>setCodeForm({...codeForm,discountType:e.target.value})}><option value="PERCENT">Percent</option><option value="FIXED">Fixed VND</option></select></label><label>Value<input type="number" min="1" value={codeForm.value} onChange={e=>setCodeForm({...codeForm,value:e.target.value})}/></label></div><label>Max discount<input type="number" min="0" value={codeForm.maxDiscount} onChange={e=>setCodeForm({...codeForm,maxDiscount:e.target.value})}/></label><label>Expires at (optional)<input type="datetime-local" value={codeForm.expiresAt} onChange={e=>setCodeForm({...codeForm,expiresAt:e.target.value})}/></label><label>Usage limit (optional)<input type="number" min="1" value={codeForm.usageLimit} onChange={e=>setCodeForm({...codeForm,usageLimit:e.target.value})}/></label><button className="adminPrimary">Create code</button></form>
        </div>

        <section className="adminPanel"><h2>Account vouchers</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Code</th><th>Account</th><th>Offer</th><th>Expiry</th><th>Status</th><th></th></tr></thead><tbody>{vouchers.map(v=>{const u=users.find(x=>x.id===v.userId);return <tr key={v.id}><td><strong>{v.code}</strong><small>{v.welcome?'Welcome voucher':'Account voucher'}</small></td><td>{u?.email||('#'+v.userId)}</td><td>{v.discountType==='PERCENT'?v.value+'%':money(v.value)}{v.maxDiscount?<small>Max {money(v.maxDiscount)}</small>:null}</td><td>{v.expiresAt?new Date(v.expiresAt).toLocaleString('vi-VN'):'No expiry'}</td><td>{v.usedAt?'USED':v.active?'ACTIVE':'INACTIVE'}</td><td>{!v.usedAt&&<button className="tableAction" onClick={()=>toggleVoucher(v)}>{v.active?'Disable':'Enable'}</button>}</td></tr>})}</tbody></table></div></section>

        <section className="adminPanel"><h2>Discount codes</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Code</th><th>Offer</th><th>Expiry</th><th>Usage</th><th>Status</th><th></th></tr></thead><tbody>{codes.map(c=><tr key={c.id}><td><strong>{c.code}</strong><small>{c.name}</small></td><td>{c.discountType==='PERCENT'?c.value+'%':money(c.value)}{c.maxDiscount?<small>Max {money(c.maxDiscount)}</small>:null}</td><td>{c.expiresAt?new Date(c.expiresAt).toLocaleString('vi-VN'):'No expiry'}</td><td>{c.usedCount}{c.usageLimit?'/'+c.usageLimit:' / ∞'}</td><td>{c.active?'ACTIVE':'INACTIVE'}</td><td><button className="tableAction" onClick={()=>toggleCode(c)}>{c.active?'Disable':'Enable'}</button></td></tr>)}</tbody></table></div></section>
      </div>}

      {tab==='reviews'&&<section className="adminPanel"><h2>Ratings & comments</h2><div className="reviewAdminList">{reviews.map(r=><article key={r.id}><div><strong>{r.bookTitle}</strong><span>{'★'.repeat(r.rating)}{'☆'.repeat(5-r.rating)}</span><small>{r.reviewerName} · {r.createdAt?new Date(r.createdAt).toLocaleDateString('vi-VN'):''}</small></div><p>{r.comment}</p><button className="dangerText" onClick={()=>deleteReview(r.id)}>Delete review</button></article>)}</div></section>}

      {tab==='staff'&&<div className="adminTwoCol compactLeft"><form className="adminPanel formPanel" onSubmit={saveStaff}><h2>{editingStaff?'Edit staff':'Add staff'}</h2><label>Full name<input name="fullName" value={staffForm.fullName} onChange={staffChange} required/></label><label>Email<input name="email" type="email" value={staffForm.email} onChange={staffChange}/></label><label>Phone<input name="phone" value={staffForm.phone} onChange={staffChange}/></label><button className="adminPrimary">{editingStaff?'Update':'Add staff'}</button>{editingStaff&&<button type="button" className="adminSecondary" onClick={()=>{setEditingStaff(null);setStaffForm(emptyStaff)}}>Cancel</button>}</form><section className="adminPanel"><h2>Staff members</h2><div className="adminTableWrap"><table className="adminTable"><thead><tr><th>Name</th><th>Email</th><th>Phone</th><th></th></tr></thead><tbody>{staff.map(s=><tr key={s.id}><td>{s.fullName}</td><td>{s.email}</td><td>{s.phone}</td><td className="rowActions"><button onClick={()=>editStaff(s)}>Edit</button><button className="dangerText" onClick={()=>deleteStaff(s.id)}>Delete</button></td></tr>)}</tbody></table></div></section></div>}
    </section>
  </main>
}
