package com.cnjava.book_store.config;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller public class SpaController { @GetMapping({"/","/admin","/admin/**"}) public String app(){return "forward:/index.html";} }
