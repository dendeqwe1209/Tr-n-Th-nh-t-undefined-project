import React,{useState}from'react';
import Brand from './Brand';

const safeNext=()=>{
  const next=new URLSearchParams(window.location.search).get('next')||'/';
  return next.startsWith('/')&&!next.startsWith('//')?next:'/';
};

export default function AuthPage({mode}){
  const isRegister=mode==='register';
  const[form,setForm]=useState({fullName:'',email:'',password:''});
  const[busy,setBusy]=useState(false),[error,setError]=useState('');
  const change=e=>setForm({...form,[e.target.name]:e.target.value});
  const next=safeNext();

  const submit=async e=>{
    e.preventDefault();setBusy(true);setError('');
    const endpoint=isRegister?'/api/auth/register':'/api/auth/login';
    const payload=isRegister?form:{email:form.email,password:form.password};
    const res=await fetch(endpoint,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
    const body=await res.json().catch(()=>({}));
    if(res.ok) window.location.href=next;
    else setError(body.message||'Authentication failed.');
    setBusy(false);
  };

  const alternate=isRegister?'login':'register';
  const altHref=`/${alternate}?next=${encodeURIComponent(next)}`;

  return <main className="authPage">
    <Brand className="authBrand"/>
    <section className="authCard">
      <span className="eyebrow">{isRegister?'CREATE ACCOUNT':'WELCOME BACK'}</span>
      <h1>{isRegister?'Join Bookstore':'Sign in'}</h1>
      <p>{isRegister?'Create an account to review books and complete orders.':'Sign in to review books and complete checkout.'}</p>
      <form onSubmit={submit}>
        {isRegister&&<label>Full name<input name="fullName" value={form.fullName} onChange={change} minLength="2" required autoComplete="name"/></label>}
        <label>Email<input name="email" type="email" value={form.email} onChange={change} required autoComplete="email"/></label>
        <label>Password<input name="password" type="password" value={form.password} onChange={change} minLength="8" required autoComplete={isRegister?'new-password':'current-password'}/></label>
        {isRegister&&<small className="authHint">Use at least 8 characters.</small>}
        {error&&<div className="authError">{error}</div>}
        <button className="authSubmit" disabled={busy}>{busy?'Please wait...':isRegister?'Create account':'Sign in'}</button>
      </form>
      <p className="authSwitch">{isRegister?'Already have an account?':'No account yet?'} <a href={altHref}>{isRegister?'Sign in':'Register'}</a></p>
    </section>
  </main>
}
