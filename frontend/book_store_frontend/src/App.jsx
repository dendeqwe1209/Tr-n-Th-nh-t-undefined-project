import React from 'react';import Store from './Store';import Admin from './Admin';export default function App(){return window.location.pathname.startsWith('/admin')?<Admin/>:<Store/>;}
