package br.com.alura.forum.controller.dto;

import java.time.LocalDateTime;

import br.com.alura.forum.modelo.Topico;

public class TopicoDto {
	private Long id;
	private String titulo;
	private String mensagem;
	private LocalDateTime dataCriacao;
	
	public TopicoDto(Topico topico) {
		this.id=topico.getId();
		this.titulo=topico.getTitulo();
		this.mensagem=topico.getMensagem();
		this.dataCriacao=topico.getDataCriacao();
	}
	
	public Long getId() {
		return id;
	}
	public String getTitulo() {
		return titulo;
	}
	public String getMensagem() {
		return mensagem;
	}
	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public static TopicoDto converter(Topico topico) { // O jackson por padrão iria converter todos os campos da entidade para o json
		TopicoDto retorno = new TopicoDto (topico);
		return retorno;
	}

}
