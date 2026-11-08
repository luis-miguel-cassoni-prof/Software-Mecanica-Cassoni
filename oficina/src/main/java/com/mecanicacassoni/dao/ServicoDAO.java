
package com.mecanicacassoni.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.mecanicacassoni.connection.ConnectionFactory;
import com.mecanicacassoni.model.Servico;

public class ServicoDAO {

    public int inserir(Servico servico) throws SQLException {
        String sql = "INSERT INTO servico (nome, id_cliente, valor_final) "
                + "VALUES (?, ?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, servico.getNome());
            ps.setInt(2, servico.getIdCliente());

            if (servico.getValorFinal() != null) {
                ps.setBigDecimal(3, servico.getValorFinal());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }

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
        String sql = "SELECT id_servico, nome, id_cliente, valor_final FROM servico";
        List<Servico> servicos = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                servicos.add(mapearServico(rs));
            }
        }

        return servicos;
    }

    public Servico buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_servico, nome, id_cliente, valor_final "
                + "FROM servico WHERE id_servico = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearServico(rs);
                }
            }
        }

        return null;
    }

    public boolean atualizar(Servico servico) throws SQLException {
        String sql = "UPDATE servico SET nome = ?, id_cliente = ?, "
                + "valor_final = ? WHERE id_servico = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, servico.getNome());
            ps.setInt(2, servico.getIdCliente());

            if (servico.getValorFinal() != null) {
                ps.setBigDecimal(3, servico.getValorFinal());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }

            ps.setInt(4, servico.getIdServico());

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

    private Servico mapearServico(ResultSet rs) throws SQLException {
        Servico servico = new Servico();

        servico.setIdServico(rs.getInt("id_servico"));
        servico.setNome(rs.getString("nome"));
        servico.setIdCliente(rs.getInt("id_cliente"));
        servico.setValorFinal(rs.getBigDecimal("valor_final"));

        return servico;
    }

    public List<Servico> listarPorAnoECliente(int idCliente, int ano) throws SQLException {
        String sql = "SELECT s.*\n" + //
        "FROM servico s\n" + //
        "WHERE s.id_cliente = ?\n" + //
        "  AND EXISTS (\n" + //
        "      SELECT 1\n" + //
        "      FROM prestacao p\n" + //
        "      WHERE p.id_servico = s.id_servico\n" + //
        "        AND p.data >= ?\n" + //
        "        AND p.data < ?\n" + //
        "  );";
        
        LocalDate dataEspecifica = LocalDate.of(ano, 01, 01);
        LocalDate finalDoFiltro = dataEspecifica.plusYears(1);

        List<Servico> servicos = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
            PreparedStatement ps = conexao.prepareStatement(sql);
            ) {

            ps.setInt(1, idCliente);
            ps.setDate(2, java.sql.Date.valueOf(dataEspecifica));
            ps.setDate(3, java.sql.Date.valueOf(finalDoFiltro));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    servicos.add(mapearServico(rs));
                }
            }
        }

        return servicos;
    }
}
