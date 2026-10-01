import React from 'react';

export default function Brand({className='brand'}){
  return <a className={className+' bookstoreBrand'} href="/" aria-label="Bookstore home">
    <svg className="bookstoreIcon" viewBox="0 0 28 28" aria-hidden="true">
      <path d="M4.5 5.5c3.7-.9 6.6-.4 9.5 1.8v15.1c-2.9-2.1-5.8-2.6-9.5-1.7V5.5Z"/>
      <path d="M23.5 5.5c-3.7-.9-6.6-.4-9.5 1.8v15.1c2.9-2.1 5.8-2.6 9.5-1.7V5.5Z"/>
      <path d="M14 7.3v15.1"/>
    </svg>
    <span>BOOKSTORE</span>
  </a>;
}
