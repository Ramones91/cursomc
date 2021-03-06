package com.ramon.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ramon.cursomc.domain.ItemPedido;
import com.ramon.cursomc.domain.PagamentoComBoleto;
import com.ramon.cursomc.domain.Pedido;
import com.ramon.cursomc.domain.enums.EstadoPagamento;
import com.ramon.cursomc.repositories.ClienteRepository;
import com.ramon.cursomc.repositories.ItemPedidoRepository;
import com.ramon.cursomc.repositories.PagamentoRepository;
import com.ramon.cursomc.repositories.PedidoRepository;
import com.ramon.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repo;
	@Autowired
	private BoletoService boletoService;
	@Autowired
	private PagamentoRepository pagtorepo;
	@Autowired
	private ProdutoService produtoService;
	@Autowired
	private ItemPedidoRepository iprepo;
	@Autowired
	private ClienteService clienteService;
	@Autowired
	private EmailService emailService;
	
	
	public Pedido find(Integer id) {
		Optional<Pedido> obj= repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}
	
	@Transactional
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);
		pagtorepo.save(obj.getPagamento());
		for(ItemPedido ip : obj.getItems()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(produtoService.find(ip.getProduto().getId()).getPreco());
			ip.setPedido(obj);
		}
		iprepo.saveAll(obj.getItems());
		emailService.sendOrderConfirmationHtmlEmail(obj);
		return obj;
	}
	
}
