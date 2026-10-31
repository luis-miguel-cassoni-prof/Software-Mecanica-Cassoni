package com.mecanicacassoni.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.mecanicacassoni.connection.ConnectionFactory;
import com.mecanicacassoni.model.Prestacao;

public class PrestacaoDAO {

    public int inserir(Prestacao prestacao) throws SQLException {
        String sql = "INSERT INTO prestacao (id_servico, id_funcionario, data, hora_inicio, hora_fim, vlr_material) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (prestacao.getIdServico() != null) {
                ps.setInt(1, prestacao.getIdServico());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            if (prestacao.getIdFuncionario() != null) {
                ps.setInt(2, prestacao.getIdFuncionario());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setDate(3, java.sql.Date.valueOf(prestacao.getData()));
            ps.setTime(4, java.sql.Time.valueOf(prestacao.getHoraInicio()));
            ps.setTime(5, java.sql.Time.valueOf(prestacao.getHoraFim()));
            ps.setBigDecimal(6, prestacao.getVlrMaterial());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Não foi possível obter o ID da prestação inserida.");
    }

    public List<Prestacao> listarTodos() throws SQLException {
        String sql = "SELECT id_prestacao, id_servico, id_funcionario, data, hora_inicio, hora_fim, vlr_material "
                   + "FROM prestacao";

        List<Prestacao> prestacoes = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Prestacao prestacao = new Prestacao();

                prestacao.setIdPrestacao(rs.getInt("id_prestacao"));

                int idServico = rs.getInt("id_servico");
                prestacao.setIdServico(rs.wasNull() ? null : idServico);

                int idFuncionario = rs.getInt("id_funcionario");
                prestacao.setIdFuncionario(rs.wasNull() ? null : idFuncionario);

                prestacao.setData(rs.getDate("data").toLocalDate());
                prestacao.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
                prestacao.setHoraFim(rs.getTime("hora_fim").toLocalTime());
                prestacao.setVlrMaterial(rs.getBigDecimal("vlr_material"));

                prestacoes.add(prestacao);
            }
        }

        return prestacoes;
    }

    public Prestacao buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_prestacao, id_servico, id_funcionario, data, hora_inicio, hora_fim, vlr_material "
                   + "FROM prestacao WHERE id_prestacao = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Prestacao prestacao = new Prestacao();

                    prestacao.setIdPrestacao(rs.getInt("id_prestacao"));

                    int idServico = rs.getInt("id_servico");
                    prestacao.setIdServico(rs.wasNull() ? null : idServico);

                    int idFuncionario = rs.getInt("id_funcionario");
                    prestacao.setIdFuncionario(rs.wasNull() ? null : idFuncionario);

                    prestacao.setData(rs.getDate("data").toLocalDate());
                    prestacao.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
                    prestacao.setHoraFim(rs.getTime("hora_fim").toLocalTime());
                    prestacao.setVlrMaterial(rs.getBigDecimal("vlr_material"));

                    return prestacao;
                }
            }
        }

        return null;
    }

    public boolean atualizar(Prestacao prestacao) throws SQLException {
        String sql = "UPDATE prestacao SET id_servico = ?, id_funcionario = ?, data = ?, "
                   + "hora_inicio = ?, hora_fim = ?, vlr_material = ? WHERE id_prestacao = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            if (prestacao.getIdServico() != null) {
                ps.setInt(1, prestacao.getIdServico());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            if (prestacao.getIdFuncionario() != null) {
                ps.setInt(2, prestacao.getIdFuncionario());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setDate(3, java.sql.Date.valueOf(prestacao.getData()));
            ps.setTime(4, java.sql.Time.valueOf(prestacao.getHoraInicio()));
            ps.setTime(5, java.sql.Time.valueOf(prestacao.getHoraFim()));
            ps.setBigDecimal(6, prestacao.getVlrMaterial());
            ps.setInt(7, prestacao.getIdPrestacao());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletarPorId(int id) throws SQLException {
        String sql = "DELETE FROM prestacao WHERE id_prestacao = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }
}