package br.com.alura.forum.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.alura.forum.repository.UsuarioRepository;

@EnableWebSecurity // Só habilitando por padrão todos os endpoints já estarão bloqueados
@Configuration
public class SecurityConfigurations extends WebSecurityConfigurerAdapter { //Esta classe de configuração é a opção ao application.properties
	
	@Autowired
	private AutenticacaoService autenticacao;
	
	@Autowired //Injetar aqui e passar como parâmetro no construtor
	private UsuarioRepository repository;
	
	@Autowired
	TokenService tokenService; //Só para passar para o construtor da AutenticacaoViaTokenFilter
	
	//Configura a autenticacao ou controle de acesso
	//A primeira implementacao do curso utiliza via sessao, que não é a forma correta para o REST
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(autenticacao).passwordEncoder(new BCryptPasswordEncoder());
	}
	
	@Override
	@Bean
		protected AuthenticationManager authenticationManager() throws Exception {
			return super.authenticationManager();
		}
	
	//Configuracoes de autorizacao, URL, quem pode ou não, perfil
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers(HttpMethod.GET, "/topicos").permitAll() //Libera a consulta da lista
		.antMatchers(HttpMethod.GET, "/topicos/*").permitAll()
		.antMatchers(HttpMethod.POST, "/auth").permitAll()
		.anyRequest().authenticated() //Qualquer outra requisicao precisa estar autenticado
		//.and().formLogin(); //Redireciona para pagina de login do sprint boot com a criação de uma session. Com a utilização do jwt não será mais utilizada.
		.and().csrf().disable()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //Não cria sessão, para usar em conjunto com o token
		.and().addFilterBefore(new AutenticacaoViaTokenFilter(tokenService, repository), UsernamePasswordAuthenticationFilter.class); //Registrando o filtro criado e definindo para ser executado antes do padrão
	}

	//Configuracoes de recursos estaticos, css, js imagens etc
	@Override
		public void configure(WebSecurity web) throws Exception {
			super.configure(web);
		}

	public static void main(String[] args) {
		System.out.println(new BCryptPasswordEncoder().encode("123456")); //Gerar o hash dessa senha
	}
}
