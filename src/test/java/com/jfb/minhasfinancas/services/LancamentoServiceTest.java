package com.jfb.minhasfinancas.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.jfb.minhasfinancas.exceptions.RegraNegocioException;
import com.jfb.minhasfinancas.model.entity.Lancamento;
import com.jfb.minhasfinancas.model.entity.Usuario;
import com.jfb.minhasfinancas.model.enums.StatusLancamento;
import com.jfb.minhasfinancas.repositories.LancamentoRepository;
import com.jfb.minhasfinancas.repositories.LancamentoRepositoryTest;
import com.jfb.minhasfinancas.services.impl.LancamentoServiceImpl;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

	@SpyBean
	LancamentoServiceImpl service;
	
	@MockBean
	LancamentoRepository repository;
	
	@Test
	public void deveSalvarUmLancamento() {
		// cenário
		Lancamento objASalvar = LancamentoRepositoryTest.criarLancamento();
		doNothing().when(service).validar(objASalvar);
		
		Lancamento objSalvo = LancamentoRepositoryTest.criarLancamento();
		objSalvo.setId(1l);
		objSalvo.setStatus(StatusLancamento.PENDENTE);
		when(repository.save(objASalvar)).thenReturn(objSalvo);
		
		// Ação/execução
		Lancamento obj = service.salvar(objASalvar);
		
		// verificação
		assertThat( obj.getId() ).isEqualTo(objSalvo.getId());
		assertThat(obj.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
	}
	
	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		// Cenário
		Lancamento objASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(objASalvar);
		
		// Ação/execução
		Assertions.catchThrowableOfType(() -> service.salvar(objASalvar),
				RegraNegocioException.class);
		
		// Verificação
		Mockito.verify(repository, Mockito.never()).save(objASalvar);
	}
	
	@Test
	public void deveAtualizarUmlancamento() {
		// Cenário
		Lancamento objSalvo = LancamentoRepositoryTest.criarLancamento();
		objSalvo.setId(1l);
		objSalvo.setStatus(StatusLancamento.PENDENTE);
		
		Mockito.doNothing().when(service).validar(objSalvo);
		Mockito.when(repository.save(objSalvo)).thenReturn(objSalvo);
		
		// Ação/execução
		service.atualizar(objSalvo);
		
		// Verificação
		Mockito.verify(repository, Mockito.times(1)).save(objSalvo);
	}
	
	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
		// Cenário
		Lancamento obj = LancamentoRepositoryTest.criarLancamento();
		
		// Ação/execução e Execução
		Assertions.catchThrowableOfType(() -> service.atualizar(obj), NullPointerException.class);
		Mockito.verify(repository, Mockito.never()).save(obj);
	}
	
	@Test
	public void deveDeletarUmLancamento() {
		// Cenário
		Lancamento obj = LancamentoRepositoryTest.criarLancamento();
		obj.setId(1l);
		
		// Ação/execução
		service.deletar(obj);
		
		// Verificação
		Mockito.verify(repository).delete(obj);
	}
	
	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
		// Cenário
		Lancamento obj = LancamentoRepositoryTest.criarLancamento();
		
		// Ação/execução
		Assertions.catchThrowableOfType(() -> service.deletar(obj), NullPointerException.class);
		
		// Verificação
		Mockito.verify(repository, Mockito.never()).delete(obj);
	}
	
	@Test
	public void deveFiltrarLancamentos() {
		// Cenário
		Lancamento obj = LancamentoRepositoryTest.criarLancamento();
		obj.setId(1l);
		
		List<Lancamento> lista = Arrays.asList(obj);
		Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lista);
		
		// Ação/execução
		List<Lancamento> resultado = service.buscar(obj);
		
		// Verificação
		Assertions.assertThat(resultado)
			.isNotEmpty()
			.hasSize(1)
			.contains(obj);
	}
	
	@Test
	public void deveAtualizarOStatusDeUmLancamento() {
		// Cenário
		Lancamento obj = LancamentoRepositoryTest.criarLancamento();
		obj.setId(1l);
		obj.setStatus(StatusLancamento.PENDENTE);
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
		Mockito.doReturn(obj).when(service).atualizar(obj);
		
		// Ação/execução 
		service.atualizarStatus(obj, novoStatus);
		
		// Verificação
		Assertions.assertThat(obj.getStatus()).isEqualTo(novoStatus);
		Mockito.verify(service).atualizar(obj);
	}
	
	@Test
	public void deveObterUmLancamentoPorId() {
		// Cenário
		Long id = 1l;
		Lancamento obj = LancamentoRepositoryTest.criarLancamento();
		obj.setId(id);
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(obj));
		
		// Ação/execução
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		// Verificação
		Assertions.assertThat(resultado.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetornaVazioQuandoUmLancamentoNaoExistir () {
		// Cenário
		Long id = 1l;
		Lancamento obj = LancamentoRepositoryTest.criarLancamento();
		obj.setId(id);
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		// Ação/execução
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		// Verificação
		Assertions.assertThat(resultado.isPresent()).isFalse();
	}
	
	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		Lancamento lancamento = new Lancamento();
		
		Throwable erro = Assertions.catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		
		lancamento.setDescricao("");
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		
		lancamento.setDescricao("Salario");
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		lancamento.setAno(0);
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		lancamento.setAno(13);
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		lancamento.setMes(1);
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(202);
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(2020);
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário válido.");
		
		lancamento.setUsuario(new Usuario());
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário válido.");
		
		lancamento.getUsuario().setId(1l);
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		
		lancamento.setValor(BigDecimal.ZERO);
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		
		lancamento.setValor(BigDecimal.valueOf(1));
		
		erro = catchThrowable( () -> service.validar(lancamento) );
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um tipo de Lancamento.");
	}
	
	
	
}













