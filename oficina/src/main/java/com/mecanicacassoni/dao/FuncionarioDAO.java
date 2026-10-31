package com.mecanicacassoni.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mecanicacassoni.connection.ConnectionFactory;
import com.mecanicacassoni.model.Funcionario;

public class FuncionarioDAO {

    public int inserir(Funcionario funcionario) throws SQLException {
        String sql = "INSERT INTO funcionario (nome) VALUES (?)";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, funcionario.getNome());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Não foi possível obter o ID do funcionário inserido.");
    }

    public List<Funcionario> listarTodos() throws SQLException {
        String sql = "SELECT id_funcionario, nome FROM funcionario";
        List<Funcionario> funcionarios = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement ps = conexao.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Funcionario funcionario = new Funcionario();

                funcionario.setIdFuncionario(rs.getInt("id_funcionario"));
                funcionario.setNome(rs.getString("nome"));

                funcionarios.add(funcionario);
            }
        }

        return funcionarios;
    }

    public boolean deletarPorId(int id) throws SQLException {
        String sql = "DELETE FROM funcionario WHERE id_funcionario = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    public Funcionario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_funcionario, nome FROM funcionario WHERE id_funcionario = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Funcionario funcionario = new Funcionario();

                    funcionario.setIdFuncionario(rs.getInt("id_funcionario"));
                    funcionario.setNome(rs.getString("nome"));

                    return funcionario;
                }
            }
        }

        return null;
    }

    public boolean atualizar(Funcionario funcionario) throws SQLException {
        String sql = "UPDATE funcionario SET nome = ? WHERE id_funcionario = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, funcionario.getNome());
            ps.setInt(2, funcionario.getIdFuncionario());

            return ps.executeUpdate() > 0;
        }
    }
}