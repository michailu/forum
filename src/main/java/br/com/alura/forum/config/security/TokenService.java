package br.com.alura.forum.config.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import br.com.alura.forum.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenService {
	
	@Value("${forum.jws.expiration}") //Pega essa propriedade do application.properties
	private String expiration;

	@Value("${forum.jws.secret}") //Pega essa propriedade do application.properties
	private String secret;
	
	public String gerarToken(Authentication auth) {
		
		Usuario logado = (Usuario)auth.getPrincipal();
		Date hoje = new Date();
		Date dataExpira = new Date(hoje.getTime() + Long.parseLong(expiration));
		
		return Jwts.builder().setIssuer("API do Fórum Alura")
				.setSubject(logado.getId().toString())
				.setIssuedAt(hoje)
				.setExpiration(dataExpira)
				.signWith(SignatureAlgorithm.HS256, secret)
				.compact(); //Transforma em uma String
	}

	public boolean isTokenValido(String token) { //Vai decriptografar o token
		System.out.println("Token que chegou para validação:"+token);
		try {
			Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token); //o secret é usado para criptografar/decriptografar
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Long getIdUsuario(String token) {
		
		Claims claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();
		return Long.parseLong(claims.getSubject());
		
	}	
}
