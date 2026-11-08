
package com.mecanicacassoni;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import com.mecanicacassoni.dao.ServicoDAO;
import com.mecanicacassoni.model.Servico;

public class TesteFiltroServico {

    public static void main(String[] args) {

        ServicoDAO servicoDAO = new ServicoDAO();

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("========== TESTE FILTRO SERVICOS ==========");

            System.out.print("Digite o ID do cliente: ");
            int idCliente = scanner.nextInt();

            System.out.print("Digite o ano: ");
            int ano = scanner.nextInt();

            System.out.println("\n========== RESULTADOS ==========");

            List<Servico> servicos =
                    servicoDAO.listarPorAnoECliente(idCliente, ano);

            if (servicos.isEmpty()) {
                System.out.println("Nenhum serviço encontrado.");
            } else {
                for (Servico servico : servicos) {
                    System.out.println("-------------------------------");
                    System.out.println("ID: " + servico.getIdServico());
                    System.out.println("Nome: " + servico.getNome());
                    System.out.println("ID Cliente: " + servico.getIdCliente());
                    System.out.println("Valor Final: " + servico.getValorFinal());
                }
            }

            System.out.println("\nTotal encontrado: " + servicos.size());
            System.out.println("========== TESTE CONCLUIDO ==========");

        } catch (SQLException e) {
            System.err.println("Erro ao consultar o banco de dados.");
            e.printStackTrace();

        } catch (java.util.InputMismatchException e) {
            System.err.println("Informe apenas números inteiros.");
        }
    }
}
