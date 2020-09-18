package br.com.alura.forum.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.alura.forum.config.security.TokenService;
import br.com.alura.forum.controller.dto.TokenDto;
import br.com.alura.forum.controller.form.LoginForm;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController { //Vai ter a lógica de autenticação, criado para utilização do token

	@Autowired
	AuthenticationManager authManager;
	
	@Autowired
	TokenService tokenService;
	
	@PostMapping //Post para receber usuário e senha
	public ResponseEntity<TokenDto> autenticar(@RequestBody @Valid LoginForm form) {
		
		System.out.println(form.getEmail() + form.getSenha());
		
		UsernamePasswordAuthenticationToken dadosLogin = form.converter();
		
		try {
			
			Authentication auth = authManager.authenticate(dadosLogin);
			
			String token = tokenService.gerarToken(auth); //Vai gerar o token
			
			System.out.println("Token gerado pelo auth:" + token);
			
			return ResponseEntity.ok(new TokenDto(token, "Bearer")); //Bearer é um tipo de autenticacao em que o token é enviado junto	
			//O client deve guardar esse token e enviar nas próximas requisições no cabeçalho
		} catch (AuthenticationException e) {
			return ResponseEntity.badRequest().build();
		}
		
	}
}
