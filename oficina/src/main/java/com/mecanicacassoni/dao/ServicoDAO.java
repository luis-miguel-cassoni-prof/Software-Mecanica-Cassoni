package com.mecanicacassoni.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mecanicacassoni.connection.ConnectionFactory;
import com.mecanicacassoni.model.Servico;

public class ServicoDAO {

    public int inserir(Servico servico) throws SQLException {
        String sql = "INSERT INTO servico (nome, id_cliente) VALUES (?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, servico.getNome());
            ps.setInt(2, servico.getIdCliente());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Não foi possível obter o ID do serviço inserido.");
    }

    public List<Servico> listarTodos() throws SQLException {
        String sql = "SELECT id_servico, nome, id_cliente FROM servico";
        List<Servico> servicos = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Servico servico = new Servico();

                servico.setIdServico(rs.getInt("id_servico"));
                servico.setNome(rs.getString("nome"));
                servico.setIdCliente(rs.getInt("id_cliente"));

                servicos.add(servico);
            }
        }

        return servicos;
    }

    public Servico buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_servico, nome, id_cliente FROM servico WHERE id_servico = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Servico servico = new Servico();

                    servico.setIdServico(rs.getInt("id_servico"));
                    servico.setNome(rs.getString("nome"));
                    servico.setIdCliente(rs.getInt("id_cliente"));

                    return servico;
                }
            }
        }

        return null;
    }

    public boolean atualizar(Servico servico) throws SQLException {
        String sql = "UPDATE servico SET nome = ?, id_cliente = ? WHERE id_servico = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, servico.getNome());
            ps.setInt(2, servico.getIdCliente());
            ps.setInt(3, servico.getIdServico());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletarPorId(int id) throws SQLException {
        String sql = "DELETE FROM servico WHERE id_servico = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }
}