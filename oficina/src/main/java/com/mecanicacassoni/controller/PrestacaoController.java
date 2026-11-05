
package com.mecanicacassoni.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mecanicacassoni.dao.ClienteDAO;
import com.mecanicacassoni.dao.FuncionarioDAO;
import com.mecanicacassoni.dao.PrestacaoDAO;
import com.mecanicacassoni.dao.ServicoDAO;

import com.mecanicacassoni.model.Cliente;
import com.mecanicacassoni.model.Funcionario;
import com.mecanicacassoni.model.Prestacao;
import com.mecanicacassoni.model.Servico;

import com.mecanicacassoni.service.CalculadoraValorServico;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class PrestacaoController {

    @FXML private TextField txtId;
    @FXML private ComboBox<Funcionario> comboFuncionarios;
    @FXML private DatePicker dateData;
    @FXML private TextField txtHoraInicio;
    @FXML private TextField txtHoraFim;
    @FXML private TextField txtValorMaterial;

    @FXML private TableView<Prestacao> tabelaPrestacoes;

    @FXML private TableColumn<Prestacao, Integer> colunaId;
    @FXML private TableColumn<Prestacao, String> colunaFuncionario;
    @FXML private TableColumn<Prestacao, String> colunaData;
    @FXML private TableColumn<Prestacao, String> colunaHoraInicio;
    @FXML private TableColumn<Prestacao, String> colunaHoraFim;
    @FXML private TableColumn<Prestacao, String> colunaValorMaterial;

    @FXML private Label lblServicoAtual;
    @FXML private Label lblClienteAtual;
    @FXML private Label lblValorTotal;

    private final PrestacaoDAO prestacaoDAO = new PrestacaoDAO();
    private final ServicoDAO servicoDAO = new ServicoDAO();
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    private final CalculadoraValorServico calculadora =
            new CalculadoraValorServico();

    private final Map<Integer, String> nomesFuncionarios = new HashMap<>();

    private static final BigDecimal VALOR_HORA_PADRAO =
            new BigDecimal("80.00");

    private static final BigDecimal VALOR_MAXIMO =
            new BigDecimal("99999999.99");

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Locale LOCALE_BR =
            Locale.forLanguageTag("pt-BR");

    private Servico servicoAtual;

    @FXML
    private void initialize() {
        colunaId.setCellValueFactory(
                new PropertyValueFactory<>("idPrestacao")
        );

        colunaFuncionario.setCellValueFactory(dados -> {
            Integer idFuncionario =
                    dados.getValue().getIdFuncionario();

            return new ReadOnlyStringWrapper(
                    nomesFuncionarios.getOrDefault(
                            idFuncionario, "Funcionário não encontrado"
                    )
            );
        });

        colunaData.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(
                        dados.getValue().getData().format(FORMATO_DATA)
                )
        );

        colunaHoraInicio.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(
                        dados.getValue().getHoraInicio().format(FORMATO_HORA)
                )
        );

        colunaHoraFim.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(
                        dados.getValue().getHoraFim().format(FORMATO_HORA)
                )
        );

        colunaValorMaterial.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(
                        formatarMoeda(dados.getValue().getVlrMaterial())
                )
        );

        comboFuncionarios.setConverter(
                new StringConverter<Funcionario>() {
                    @Override
                    public String toString(Funcionario funcionario) {
                        return funcionario == null
                                ? ""
                                : funcionario.getNome();
                    }

                    @Override
                    public Funcionario fromString(String texto) {
                        return null;
                    }
                }
        );

        tabelaPrestacoes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, antigo, novo) -> {
                    if (novo != null) {
                        preencherCampos(novo);
                    }
                });

        carregarFuncionarios();
    }

    public void definirServico(Servico servico) {
        if (servico == null || servico.getIdServico() == null) {
            throw new IllegalArgumentException("Serviço inválido.");
        }

        this.servicoAtual = servico;

        lblServicoAtual.setText(
                "Serviço Atual: " + servico.getNome()
        );

        carregarClienteAtual();
        carregarPrestacoes();
    }

    @FXML
    private void inserirPrestacao() {
        if (!validarServicoAtual()) {
            return;
        }

        Prestacao prestacao = lerDadosFormulario("Cadastro");

        if (prestacao == null) {
            return;
        }

        prestacao.setIdServico(servicoAtual.getIdServico());
        prestacao.setValorHora(VALOR_HORA_PADRAO);

        try {
            int id = prestacaoDAO.inserir(prestacao);

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Cadastro",
                    "Prestação cadastrada com sucesso! ID: " + id
            );

            limparCampos();
            carregarPrestacoes();

        } catch (SQLException e) {
            mostrarErroSql(
                    "Cadastro",
                    "Não foi possível cadastrar a prestação.",
                    e
            );
        }
    }

    @FXML
    private void buscarPrestacaoPorId() {
        if (!validarServicoAtual()) {
            return;
        }

        Integer id = lerIdValido("Buscar");

        if (id == null) {
            return;
        }

        try {
            Prestacao prestacao = buscarPrestacaoDoServico(id, "Buscar");

            if (prestacao == null) {
                return;
            }

            preencherCampos(prestacao);

        } catch (SQLException e) {
            mostrarErroSql(
                    "Buscar",
                    "Não foi possível buscar a prestação.",
                    e
            );
        }
    }

    @FXML
    private void atualizarPrestacao() {
        if (!validarServicoAtual()) {
            return;
        }

        Integer id = lerIdValido("Atualizar");

        if (id == null) {
            return;
        }

        try {
            Prestacao existente =
                    buscarPrestacaoDoServico(id, "Atualizar");

            if (existente == null) {
                return;
            }

            Prestacao dados = lerDadosFormulario("Atualizar");

            if (dados == null) {
                return;
            }

            existente.setIdFuncionario(dados.getIdFuncionario());
            existente.setData(dados.getData());
            existente.setHoraInicio(dados.getHoraInicio());
            existente.setHoraFim(dados.getHoraFim());
            existente.setVlrMaterial(dados.getVlrMaterial());

            boolean atualizado = prestacaoDAO.atualizar(existente);

            if (!atualizado) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Atualizar",
                        "Nenhuma atualização foi confirmada."
                );
                return;
            }

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Atualizar",
                    "Prestação atualizada com sucesso!"
            );

            limparCampos();
            carregarPrestacoes();

        } catch (SQLException e) {
            mostrarErroSql(
                    "Atualizar",
                    "Não foi possível atualizar a prestação.",
                    e
            );
        }
    }

    @FXML
    private void deletarPrestacao() {
        if (!validarServicoAtual()) {
            return;
        }

        Integer id = lerIdValido("Excluir");

        if (id == null) {
            return;
        }

        try {
            Prestacao prestacao =
                    buscarPrestacaoDoServico(id, "Excluir");

            if (prestacao == null) {
                return;
            }

            Alert confirmacao = new Alert(
                    Alert.AlertType.CONFIRMATION
            );

            confirmacao.setTitle("Excluir prestação");
            confirmacao.setHeaderText("Confirmar exclusão");
            confirmacao.setContentText(
                    "Deseja realmente excluir a prestação ID "
                    + id + " deste serviço?"
            );

            boolean confirmou = confirmacao.showAndWait()
                    .filter(ButtonType.OK::equals)
                    .isPresent();

            if (!confirmou) {
                return;
            }

            boolean excluido = prestacaoDAO.deletarPorId(id);

            if (!excluido) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Excluir",
                        "Nenhuma prestação foi excluída."
                );
                return;
            }

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Excluir",
                    "Prestação excluída com sucesso!"
            );

            limparCampos();
            carregarPrestacoes();

        } catch (SQLException e) {
            mostrarErroSql(
                    "Excluir",
                    "Não foi possível excluir a prestação.",
                    e
            );
        }
    }

    private Prestacao lerDadosFormulario(String operacao) {
        Funcionario funcionario = comboFuncionarios.getValue();
        LocalDate data = dateData.getValue();

        if (funcionario == null) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    operacao,
                    "Selecione um funcionário."
            );
            return null;
        }

        if (data == null) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    operacao,
                    "Selecione uma data."
            );
            return null;
        }

        try {
            LocalTime inicio = LocalTime.parse(
                    txtHoraInicio.getText().trim(),
                    FORMATO_HORA
            );

            LocalTime fim = LocalTime.parse(
                    txtHoraFim.getText().trim(),
                    FORMATO_HORA
            );

            if (!fim.isAfter(inicio)) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        operacao,
                        "O horário final deve ser posterior ao inicial."
                );
                return null;
            }

            BigDecimal valorMaterial = lerValorMaterial(operacao);

            if (valorMaterial == null) {
                return null;
            }

            Prestacao prestacao = new Prestacao();

            prestacao.setIdFuncionario(
                    funcionario.getIdFuncionario()
            );
            prestacao.setData(data);
            prestacao.setHoraInicio(inicio);
            prestacao.setHoraFim(fim);
            prestacao.setVlrMaterial(valorMaterial);

            return prestacao;

        } catch (DateTimeParseException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    operacao,
                    "Informe os horários no formato HH:mm. Exemplo: 08:30."
            );
            return null;
        }
    }

    private BigDecimal lerValorMaterial(String operacao) {
        String texto = txtValorMaterial.getText().trim();

        if (!texto.matches("\\d+([,.]\\d{1,2})?")) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    operacao,
                    "Digite um valor válido para os materiais. Exemplo: 150,00."
            );
            return null;
        }

        try {
            BigDecimal valor = new BigDecimal(
                    texto.replace(',', '.')
            ).setScale(2);

            if (valor.compareTo(VALOR_MAXIMO) > 0) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        operacao,
                        "O valor dos materiais excede o limite permitido."
                );
                return null;
            }

            return valor;

        } catch (NumberFormatException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    operacao,
                    "O valor dos materiais é inválido."
            );
            return null;
        }
    }

    private Prestacao buscarPrestacaoDoServico(
            int id, String operacao
    ) throws SQLException {

        Prestacao prestacao = prestacaoDAO.buscarPorId(id);

        if (prestacao == null
                || !servicoAtual.getIdServico()
                        .equals(prestacao.getIdServico())) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    operacao,
                    "Prestação não encontrada neste serviço."
            );

            return null;
        }

        return prestacao;
    }

    private void carregarFuncionarios() {
        try {
            List<Funcionario> funcionarios =
                    funcionarioDAO.listarTodos();

            nomesFuncionarios.clear();

            for (Funcionario funcionario : funcionarios) {
                nomesFuncionarios.put(
                        funcionario.getIdFuncionario(),
                        funcionario.getNome()
                );
            }

            comboFuncionarios.setItems(
                    FXCollections.observableArrayList(funcionarios)
            );

            tabelaPrestacoes.refresh();

        } catch (SQLException e) {
            mostrarErroSql(
                    "Funcionários",
                    "Não foi possível carregar os funcionários.",
                    e
            );
        }
    }

    private void carregarClienteAtual() {
        try {
            Cliente cliente = clienteDAO.buscarPorId(
                    servicoAtual.getIdCliente()
            );

            String nome = cliente == null
                    ? "Cliente não encontrado"
                    : cliente.getNome();

            lblClienteAtual.setText("Cliente: " + nome);

        } catch (SQLException e) {
            mostrarErroSql(
                    "Cliente",
                    "Não foi possível carregar o cliente.",
                    e
            );
        }
    }

    private void carregarPrestacoes() {
        if (servicoAtual == null) {
            return;
        }

        try {
            List<Prestacao> prestacoes =
                    prestacaoDAO.listarPorServico(
                            servicoAtual.getIdServico()
                    );

            tabelaPrestacoes.setItems(
                    FXCollections.observableArrayList(prestacoes)
            );

            atualizarValorTotal(prestacoes);

        } catch (SQLException e) {
            mostrarErroSql(
                    "Listagem",
                    "Não foi possível carregar as prestações.",
                    e
            );
        } catch (IllegalArgumentException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Cálculo",
                    e.getMessage()
            );
        }
    }

    private void atualizarValorTotal(List<Prestacao> prestacoes)
            throws SQLException {

        Servico atualizado = servicoDAO.buscarPorId(
                servicoAtual.getIdServico()
        );

        if (atualizado == null) {
            throw new SQLException(
                    "O serviço atual não foi encontrado."
            );
        }

        servicoAtual = atualizado;

        BigDecimal totalAutomatico =
                calculadora.calcularTotalServico(prestacoes);

        BigDecimal totalFinal =
                atualizado.getValorFinal() != null
                        ? atualizado.getValorFinal()
                        : totalAutomatico;

        lblValorTotal.setText(
                "Valor Total do Serviço: " + formatarMoeda(totalFinal)
        );

        lblServicoAtual.setText(
                "Serviço Atual: " + atualizado.getNome()
        );
    }

    private void preencherCampos(Prestacao prestacao) {
        txtId.setText(
                String.valueOf(prestacao.getIdPrestacao())
        );

        dateData.setValue(prestacao.getData());

        txtHoraInicio.setText(
                prestacao.getHoraInicio().format(FORMATO_HORA)
        );

        txtHoraFim.setText(
                prestacao.getHoraFim().format(FORMATO_HORA)
        );

        txtValorMaterial.setText(
                prestacao.getVlrMaterial()
                        .setScale(2)
                        .toPlainString()
                        .replace('.', ',')
        );

        comboFuncionarios.getSelectionModel().clearSelection();

        for (Funcionario funcionario : comboFuncionarios.getItems()) {
            if (funcionario.getIdFuncionario().equals(
                    prestacao.getIdFuncionario()
            )) {
                comboFuncionarios.setValue(funcionario);
                break;
            }
        }
    }

    private Integer lerIdValido(String operacao) {
        try {
            int id = Integer.parseInt(
                    txtId.getText().trim()
            );

            if (id <= 0) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        operacao,
                        "O ID deve ser maior que zero."
                );
                return null;
            }

            return id;

        } catch (NumberFormatException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    operacao,
                    "Digite um ID numérico inteiro válido."
            );
            return null;
        }
    }

    private boolean validarServicoAtual() {
        if (servicoAtual == null) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Prestação",
                    "Nenhum serviço foi selecionado."
            );
            return false;
        }

        return true;
    }

    private void limparCampos() {
        tabelaPrestacoes.getSelectionModel().clearSelection();

        txtId.clear();
        comboFuncionarios.getSelectionModel().clearSelection();

        dateData.setValue(LocalDate.now());

        txtHoraInicio.clear();
        txtHoraFim.clear();
        txtValorMaterial.clear();
    }

    private String formatarMoeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(
                LOCALE_BR
        ).format(valor);
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensagem
    ) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErroSql(
            String operacao,
            String mensagem,
            SQLException e
    ) {
        mostrarAlerta(
                Alert.AlertType.ERROR,
                operacao,
                mensagem
        );

        e.printStackTrace();
    }
}
