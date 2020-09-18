package br.com.alura.forum.controller;

import java.net.URI;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alura.forum.controller.dto.DetalhesDoTopicoDto;
import br.com.alura.forum.controller.dto.TopicoDto;
import br.com.alura.forum.controller.form.AtualizacaoTopicoForm;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;

@RestController //Usando o RestController não precisa ficar usando o ResponseBody em todos os métodos
@RequestMapping("/topicos") //Este controller responde por essa URL, nos métodos precisa adicionar os verbos http
public class TopicosController {
	
	@Autowired
	private TopicoRepository topicoRepository;

	@Autowired
	private CursoRepository cursoRepository;
	
	@GetMapping //Requisicao GET
	//public Page<Topico> lista(@RequestParam (required = false) String nomeCurso, @RequestParam (required = true) int pag, @RequestParam (required = true) int qtd, @RequestParam String ordenacao) {
	@Cacheable(value = "listaTopicos") // Precisa avaliar os pontos para invalidar o cache
	public Page<Topico> lista(@RequestParam (required = false) String nomeCurso, @PageableDefault (sort="id", direction = Direction.ASC, page=0, size=50) Pageable paginacao) { // Se não vier como parâmetro na URL vale o default
		// Não é uma boa prática o Controller utilizar uma entidade como tipo
		//@RequestParam indica que é parâmetro de URL
		
		//Pageable paginacao = PageRequest.of(pag, qtd, Direction.DESC, ordenacao); // Vai ordenar em ordem crescente pelo campo passado como parâmetroS
		
		Page<Topico> topicos = null; // O Page retorna a lista e mais informações sobre a listagem
		
		if (nomeCurso == null) {
			topicos = topicoRepository.findAll(paginacao);
		} else {
			topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);
		}
		
		return topicos;				
		//return TopicoDto.converter(topicos); // Converte da entidade para o DTO somente com os campos desejados (VI, TO)
		// ?page=0&size=5&nomeCurso=Java&sort=dataCriacao,desc&sort=id exemplo de parâmetros passados na URL
	}
	
	@PostMapping //Requisicao POST
	@Transactional //Recomendado colocar no insert, update e delete. O @RequestBody indica para pegar o form do corpo da requisição e não da URL
	@CacheEvict(value = "listaTopicos", allEntries = true) //Limpa todos os registros do cache
	public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder) { //Se não anotar com @Valid as validacoes nao serao executadas
		Topico topico = form.converter(cursoRepository);
		topicoRepository.save(topico);
		URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri(); //Devolve a uri do novo recurso (Topico) cadastrado no servidor
		return ResponseEntity.created(uri).body(new TopicoDto(topico)); // Tudo isso para devolver o 201 do http e o id do novo recurso criado no servidor
	}
	
	@GetMapping("/{id}") //Requisicao GET
	public ResponseEntity<DetalhesDoTopicoDto> detalhar(@PathVariable Long id) { //Variavel da URL, tem que ser o mesmo nome id
		Optional<Topico> topico = topicoRepository.findById(id); //Se usar o getOne ele considera que o id existe no banco, se não existir lançará uma exceção
		if(topico.isPresent()) { //Se existe um topico no Optional
			return ResponseEntity.ok(new DetalhesDoTopicoDto(topico.get())); //Get para pegar o Topico do Optional
		} 
		
		return ResponseEntity.notFound().build();
	}
	
	@PutMapping("/{id}")
	@Transactional //Informar para o sprint que e para comitar a transacao
	@CacheEvict(value = "listaTopicos", allEntries = true) //Limpa todos os registros do cache
	public ResponseEntity<TopicoDto> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoForm form) {
		Topico topicoAtualizado = form.atualizar(id, topicoRepository, form); // Esta gerenciado e já atualizará com o set
		
		return ResponseEntity.ok(new TopicoDto(topicoAtualizado));
	}
	
	@DeleteMapping("/{id}")
	@Transactional //Se não colocar não dispara o commit da transacao
	@CacheEvict(value = "listaTopicos", allEntries = true) //Limpa todos os registros do cache
	public ResponseEntity<?> remover(@PathVariable Long id) {
		Optional<Topico> topico = topicoRepository.findById(id);
		if (topico.isPresent()) {
			topicoRepository.deleteById(id);
			return ResponseEntity.ok().build();
		}
		
		return ResponseEntity.notFound().build(); //Devolve um 200
	}
}
