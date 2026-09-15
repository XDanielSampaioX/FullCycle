package com.biblioteca.Biblioteca.Online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class BibliotecaOnlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(BibliotecaOnlineApplication.class, args);
	}

}
