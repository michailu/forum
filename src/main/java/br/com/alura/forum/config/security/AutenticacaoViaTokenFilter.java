package br.com.alura.forum.config.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.alura.forum.modelo.Usuario;
import br.com.alura.forum.repository.UsuarioRepository;

public class AutenticacaoViaTokenFilter extends OncePerRequestFilter { //Vai interceptar as requisições e validar o token

	//@Autowired Não pode ser feito aqui, filtro não recebe injeção de dependência
	TokenService tokenService;
	UsuarioRepository repository;
	
	public AutenticacaoViaTokenFilter(TokenService tokenService, UsuarioRepository repository) {
		this.tokenService = tokenService;
		this.repository = repository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		//O token tem que ser validado a cada requisição, não existe o conceito de usuário logado no REST
		//1 - Recuperando o token do request
		String token = recuperarToken(request);
		
		System.out.println("Passando pelo filtro:" + token);
		
		if (token != null) { //Mesmo com as URLs liberadas ele passa pelo filtro
			System.out.println("Token pego na validação dentro do filtro:" + token);
			
			//2 - Validar o token
			boolean valido = tokenService.isTokenValido(token);
			
			if (valido) {
				System.out.println("Token é válido:" + valido);
				autenticarCliente(token);
			}
		}
	
		filterChain.doFilter(request, response); //Se não autenticar vai cair aqui e o sprint vai barrar a requisicao
	}

	private void autenticarCliente(String token) { //Vai forçar a autenticação no spring após token estar validado
		Long idUsuario = tokenService.getIdUsuario(token);
		System.out.println("Id do usuario:"+idUsuario);
		Usuario usuario = repository.findById(idUsuario).get(); //O getone não instanciava
		System.out.println("Nome do Usuario:"+usuario.getNome() + " Email:" +usuario.getEmail());
		UsernamePasswordAuthenticationToken autenthication = new UsernamePasswordAuthenticationToken(idUsuario, null, usuario.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(autenthication); //Força que usuário está atenticado
	}
	
	private String recuperarToken(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token == null || token.isEmpty() || !token.startsWith("Bearer ")) {
			return null;
		}
		return token.substring(7, token.length()); //Remove o início 'Bearer '
	}
}
