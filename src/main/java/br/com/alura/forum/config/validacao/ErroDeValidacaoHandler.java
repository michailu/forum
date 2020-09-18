package br.com.alura.forum.config.validacao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice //Intercepta as excecoes das classes de controle REST
public class ErroDeValidacaoHandler { //Interceptor para tratamento de excecoes

	@Autowired //Para injetar
	private MessageSource messageSource; //Ajuda no tratamento da excecao, usa o idioma do cliente

	@ResponseStatus(code = HttpStatus.BAD_REQUEST) // Se não fizer essa configuracao vai devolver 200 para o cliente
	@ExceptionHandler(MethodArgumentNotValidException.class) //Para tratar os erros de validacao de formulario 
	public List<ErroDeFormularioDto> handle(MethodArgumentNotValidException exception) {

		List<ErroDeFormularioDto> dto = new ArrayList<>();
		List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
		fieldErrors.forEach(e -> {
			String mensagem = messageSource.getMessage(e, LocaleContextHolder.getLocale()); //Detecta o idioma adequado para mensagem de erro
			ErroDeFormularioDto erro = new ErroDeFormularioDto(e.getField(), mensagem);
			dto.add(erro);
		});
		return dto;
	}
}
