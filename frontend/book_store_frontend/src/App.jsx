import React from 'react';
import Store from './Store';
import Admin from './Admin';
import ProductDetail from './ProductDetail';
import Checkout from './Checkout';
import AuthPage from './AuthPage';
import SupportChat from './SupportChat';

export default function App(){
  const path=window.location.pathname;
  if(path.startsWith('/admin')) return <Admin/>;
  if(path==='/login') return <AuthPage mode="login"/>;
  if(path==='/register') return <AuthPage mode="register"/>;
  if(path==='/checkout') return <><Checkout/><SupportChat/></>;
  const match=path.match(/^\/books\/(\d+)\/?$/);
  if(match) return <><ProductDetail bookId={match[1]}/><SupportChat/></>;
  return <><Store/><SupportChat/></>;
}
