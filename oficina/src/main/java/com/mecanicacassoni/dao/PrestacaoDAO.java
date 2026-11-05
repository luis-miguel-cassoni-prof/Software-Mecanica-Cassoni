
package com.mecanicacassoni.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mecanicacassoni.connection.ConnectionFactory;
import com.mecanicacassoni.model.Prestacao;

public class PrestacaoDAO {

    private static final BigDecimal VALOR_HORA_PADRAO = new BigDecimal("80.00");

    public int inserir(Prestacao prestacao) throws SQLException {
        String sql = "INSERT INTO prestacao "
                + "(id_servico, id_funcionario, data, hora_inicio, hora_fim, vlr_material, valor_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, Objects.requireNonNull(prestacao.getIdServico()));
            ps.setInt(2, Objects.requireNonNull(prestacao.getIdFuncionario()));
            ps.setDate(3, java.sql.Date.valueOf(prestacao.getData()));
            ps.setTime(4, java.sql.Time.valueOf(prestacao.getHoraInicio()));
            ps.setTime(5, java.sql.Time.valueOf(prestacao.getHoraFim()));
            ps.setBigDecimal(6, prestacao.getVlrMaterial());

            BigDecimal valorHora = prestacao.getValorHora() != null
                    ? prestacao.getValorHora()
                    : VALOR_HORA_PADRAO;

            ps.setBigDecimal(7, valorHora);

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
        String sql = "SELECT id_prestacao, id_servico, id_funcionario, "
                + "data, hora_inicio, hora_fim, vlr_material, valor_hora FROM prestacao";

        List<Prestacao> prestacoes = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestacoes.add(mapearPrestacao(rs));
            }
        }

        return prestacoes;
    }

    public Prestacao buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_prestacao, id_servico, id_funcionario, "
                + "data, hora_inicio, hora_fim, vlr_material, valor_hora "
                + "FROM prestacao WHERE id_prestacao = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestacao(rs);
                }
            }
        }

        return null;
    }

    public boolean atualizar(Prestacao prestacao) throws SQLException {
        String sql = "UPDATE prestacao SET id_servico = ?, id_funcionario = ?, "
                + "data = ?, hora_inicio = ?, hora_fim = ?, "
                + "vlr_material = ?, valor_hora = ? WHERE id_prestacao = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, Objects.requireNonNull(prestacao.getIdServico()));
            ps.setInt(2, Objects.requireNonNull(prestacao.getIdFuncionario()));
            ps.setDate(3, java.sql.Date.valueOf(prestacao.getData()));
            ps.setTime(4, java.sql.Time.valueOf(prestacao.getHoraInicio()));
            ps.setTime(5, java.sql.Time.valueOf(prestacao.getHoraFim()));
            ps.setBigDecimal(6, prestacao.getVlrMaterial());
            ps.setBigDecimal(7, Objects.requireNonNull(prestacao.getValorHora()));
            ps.setInt(8, Objects.requireNonNull(prestacao.getIdPrestacao()));

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

    public List<Prestacao> listarPorServico(int idServico) throws SQLException {
        String sql = "SELECT id_prestacao, id_servico, id_funcionario, "
                + "data, hora_inicio, hora_fim, vlr_material, valor_hora "
                + "FROM prestacao WHERE id_servico = ? "
                + "ORDER BY data DESC, hora_inicio DESC";

        List<Prestacao> prestacoes = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, idServico);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestacoes.add(mapearPrestacao(rs));
                }
            }
        }

        return prestacoes;
    }

    private Prestacao mapearPrestacao(ResultSet rs) throws SQLException {
        Prestacao prestacao = new Prestacao();

        prestacao.setIdPrestacao(rs.getInt("id_prestacao"));
        prestacao.setIdServico(rs.getInt("id_servico"));
        prestacao.setIdFuncionario(rs.getInt("id_funcionario"));
        prestacao.setData(rs.getDate("data").toLocalDate());
        prestacao.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        prestacao.setHoraFim(rs.getTime("hora_fim").toLocalTime());
        prestacao.setVlrMaterial(rs.getBigDecimal("vlr_material"));
        prestacao.setValorHora(rs.getBigDecimal("valor_hora"));

        return prestacao;
    }
}
