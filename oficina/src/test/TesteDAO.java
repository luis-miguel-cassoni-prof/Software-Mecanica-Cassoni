
package com.mecanicacassoni;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.SQLException;
import java.util.List;

import com.mecanicacassoni.dao.ClienteDAO;
import com.mecanicacassoni.dao.FuncionarioDAO;
import com.mecanicacassoni.dao.ServicoDAO;
import com.mecanicacassoni.dao.PrestacaoDAO;

import com.mecanicacassoni.model.Cliente;
import com.mecanicacassoni.model.Funcionario;
import com.mecanicacassoni.model.Servico;
import com.mecanicacassoni.model.Prestacao;

public class TesteDAO {

    public static void main(String[] args) {

        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        ServicoDAO servicoDAO = new ServicoDAO();
        PrestacaoDAO prestacaoDAO = new PrestacaoDAO();

        try {
            System.out.println("========== TESTE FUNCIONARIO ==========");

            Funcionario funcionario = new Funcionario();
            funcionario.setNome("Funcionario Teste");

            int idFuncionario = funcionarioDAO.inserir(funcionario);
            System.out.println("Inserir: ID " + idFuncionario);

            List<Funcionario> funcionarios = funcionarioDAO.listarTodos();
            System.out.println("Listar: " + funcionarios.size() + " registros");

            Funcionario funcionarioBuscado = funcionarioDAO.buscarPorId(idFuncionario);
            System.out.println("Buscar: " + funcionarioBuscado.getNome());

            funcionarioBuscado.setNome("Funcionario Atualizado");
            System.out.println("Atualizar: " + funcionarioDAO.atualizar(funcionarioBuscado));

            System.out.println("Excluir: " + funcionarioDAO.deletarPorId(idFuncionario));


            System.out.println("\n========== TESTE CLIENTE ==========");

            Cliente cliente = new Cliente();
            cliente.setNome("Cliente Teste");
            cliente.setCpf("999.999.999-99");

            int idCliente = clienteDAO.inserir(cliente);
            System.out.println("Inserir: ID " + idCliente);

            List<Cliente> clientes = clienteDAO.listarTodos();
            System.out.println("Listar: " + clientes.size() + " registros");

            Cliente clienteBuscado = clienteDAO.buscarPorId(idCliente);
            System.out.println("Buscar: " + clienteBuscado.getNome());

            clienteBuscado.setNome("Cliente Atualizado");
            System.out.println("Atualizar: " + clienteDAO.atualizar(clienteBuscado));


            System.out.println("\n========== TESTE SERVICO ==========");

            Servico servico = new Servico();
            servico.setNome("Servico Teste");
            servico.setIdCliente(idCliente);

            int idServico = servicoDAO.inserir(servico);
            System.out.println("Inserir: ID " + idServico);

            List<Servico> servicos = servicoDAO.listarTodos();
            System.out.println("Listar: " + servicos.size() + " registros");

            Servico servicoBuscado = servicoDAO.buscarPorId(idServico);
            System.out.println("Buscar: " + servicoBuscado.getNome());

            servicoBuscado.setNome("Servico Atualizado");
            System.out.println("Atualizar: " + servicoDAO.atualizar(servicoBuscado));


            System.out.println("\n========== TESTE PRESTACAO ==========");

            Prestacao prestacao = new Prestacao();
            prestacao.setIdServico(idServico);
            prestacao.setIdFuncionario(idFuncionario);
            prestacao.setData(LocalDate.now());
            prestacao.setHoraInicio(LocalTime.of(8, 0));
            prestacao.setHoraFim(LocalTime.of(10, 30));
            prestacao.setVlrMaterial(new BigDecimal("150.50"));

            int idPrestacao = prestacaoDAO.inserir(prestacao);
            System.out.println("Inserir: ID " + idPrestacao);

            List<Prestacao> prestacoes = prestacaoDAO.listarTodos();
            System.out.println("Listar: " + prestacoes.size() + " registros");

            Prestacao prestacaoBuscada = prestacaoDAO.buscarPorId(idPrestacao);
            System.out.println("Buscar: " + prestacaoBuscada.getVlrMaterial());

            prestacaoBuscada.setVlrMaterial(new BigDecimal("200.00"));
            System.out.println("Atualizar: " + prestacaoDAO.atualizar(prestacaoBuscada));

            System.out.println("Excluir prestação: " + prestacaoDAO.deletarPorId(idPrestacao));
            System.out.println("Excluir serviço: " + servicoDAO.deletarPorId(idServico));
            System.out.println("Excluir cliente: " + clienteDAO.deletarPorId(idCliente));

            System.out.println("\n========== TESTES CONCLUIDOS ==========");

        } catch (SQLException e) {
            System.out.println("Erro durante os testes dos DAOs:");
            e.printStackTrace();
        }
    }
}
