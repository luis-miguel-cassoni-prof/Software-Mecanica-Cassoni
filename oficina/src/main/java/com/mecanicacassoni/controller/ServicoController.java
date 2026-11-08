
package com.mecanicacassoni.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mecanicacassoni.dao.ClienteDAO;
import com.mecanicacassoni.dao.PrestacaoDAO;
import com.mecanicacassoni.dao.ServicoDAO;
import com.mecanicacassoni.model.Cliente;
import com.mecanicacassoni.model.Prestacao;
import com.mecanicacassoni.model.Servico;
import com.mecanicacassoni.service.CalculadoraValorServico;
import com.mecanicacassoni.util.Navegacao;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ServicoController {

    @FXML private TextField txtId;
    @FXML private TextField txtNome;
    @FXML private TextField txtValor;

    @FXML private ComboBox<Cliente> comboClientes;
    @FXML private TableView<Servico> tabelaServicos;

    @FXML private TableColumn<Servico, Integer> colunaId;
    @FXML private TableColumn<Servico, String> colunaNome;
    @FXML private TableColumn<Servico, String> colunaCliente;
    @FXML private TableColumn<Servico, String> colunaValorTotal;

    private final ServicoDAO servicoDAO = new ServicoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PrestacaoDAO prestacaoDAO = new PrestacaoDAO();

    private final CalculadoraValorServico calculadora =
            new CalculadoraValorServico();

    private final Map<Integer, String> nomesClientes = new HashMap<>();
    private final Map<Integer, BigDecimal> totaisAutomaticos = new HashMap<>();

    private Integer idServicoExibido;
    private BigDecimal valorExibidoOriginal;

    @FXML
    private void initialize() {
        colunaId.setCellValueFactory(
                new PropertyValueFactory<>("idServico")
        );

        colunaNome.setCellValueFactory(
                new PropertyValueFactory<>("nome")
        );

        colunaCliente.setCellValueFactory(dados -> {
            Integer idCliente = dados.getValue().getIdCliente();

            String nome = nomesClientes.getOrDefault(
                    idCliente, "Cliente não encontrado"
            );

            return new ReadOnlyStringWrapper(nome);
        });

        colunaValorTotal.setCellValueFactory(dados -> {
            BigDecimal valor = obterValorEfetivo(dados.getValue());
            return new ReadOnlyStringWrapper(formatarMoeda(valor));
        });

        comboClientes.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente == null ? "" : cliente.getNome();
            }

            @Override
            public Cliente fromString(String texto) {
                return null;
            }
        });

        tabelaServicos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, antigo, novo) -> {
                    if (novo != null) {
                        preencherCampos(novo);
                    }
                });

        carregarClientes();
        carregarServicos();
    }

    @FXML
    private void inserirServico() {
        String nome = txtNome.getText().trim();
        Cliente cliente = comboClientes.getValue();

        if (!validarNome(nome, "Cadastro")
                || !validarCliente(cliente, "Cadastro")) {
            return;
        }

        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setIdCliente(cliente.getIdCliente());
        servico.setValorFinal(null);

        try {
            int id = servicoDAO.inserir(servico);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Cadastro",
                    "Serviço cadastrado com sucesso! ID: " + id
                    + ". O valor será calculado pelas prestações.");

            limparCampos();
            carregarServicos();

        } catch (SQLException e) {
            mostrarErroSql("Cadastro",
                    "Não foi possível cadastrar o serviço.", e);
        }
    }

    @FXML
    private void buscarServicoPorId() {
        Integer id = lerIdValido("Buscar");

        if (id == null) {
            return;
        }

        try {
            carregarTotaisAutomaticos();

            Servico servico = servicoDAO.buscarPorId(id);

            if (servico == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Buscar",
                        "Serviço não encontrado.");
                return;
            }

            preencherCampos(servico);
            tabelaServicos.refresh();

        } catch (SQLException e) {
            mostrarErroSql("Buscar",
                    "Não foi possível buscar o serviço.", e);
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Buscar", e.getMessage());
        }
    }

    @FXML
    private void atualizarServico() {
        Integer id = lerIdValido("Atualizar");

        if (id == null) {
            return;
        }

        String nome = txtNome.getText().trim();
        Cliente cliente = comboClientes.getValue();

        if (!validarNome(nome, "Atualizar")
                || !validarCliente(cliente, "Atualizar")) {
            return;
        }

        if (idServicoExibido == null
                || !idServicoExibido.equals(id)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atualizar",
                    "Busque ou selecione o serviço antes de atualizá-lo.");
            return;
        }

        BigDecimal valorInformado = lerValorMonetario("Atualizar");

        if (valorInformado == null) {
            return;
        }

        try {
            Servico servico = servicoDAO.buscarPorId(id);

            if (servico == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atualizar",
                        "Serviço não encontrado.");
                return;
            }

            servico.setNome(nome);
            servico.setIdCliente(cliente.getIdCliente());

            if (valorExibidoOriginal == null
                    || valorInformado.compareTo(valorExibidoOriginal) != 0) {
                servico.setValorFinal(valorInformado);
            }

            boolean atualizado = servicoDAO.atualizar(servico);

            if (!atualizado) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atualizar",
                        "Nenhuma atualização foi confirmada.");
                return;
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Atualizar",
                    "Serviço atualizado com sucesso!");

            limparCampos();
            carregarServicos();

        } catch (SQLException e) {
            mostrarErroSql("Atualizar",
                    "Não foi possível atualizar o serviço.", e);
        }
    }

    @FXML
    private void restaurarValorAutomatico() {
        Integer id = lerIdValido("Restaurar valor");

        if (id == null) {
            return;
        }

        if (idServicoExibido == null
                || !idServicoExibido.equals(id)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Restaurar valor",
                    "Busque ou selecione o serviço antes de restaurar.");
            return;
        }

        try {
            Servico servico = servicoDAO.buscarPorId(id);

            if (servico == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Restaurar valor",
                        "Serviço não encontrado.");
                return;
            }

            if (servico.getValorFinal() == null) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Restaurar valor",
                        "Este serviço já utiliza o cálculo automático.");
                return;
            }

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Restaurar valor automático");
            confirmacao.setHeaderText("Confirmar restauração");
            confirmacao.setContentText(
                    "Deseja remover o preço manual do serviço "
                    + servico.getNome() + "?"
            );

            boolean confirmou = confirmacao.showAndWait()
                    .filter(ButtonType.OK::equals)
                    .isPresent();

            if (!confirmou) {
                return;
            }

            servico.setValorFinal(null);

            if (!servicoDAO.atualizar(servico)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Restaurar valor",
                        "A restauração não foi confirmada.");
                return;
            }

            carregarServicos();
            preencherCampos(servico);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Restaurar valor",
                    "O cálculo automático foi restaurado.");

        } catch (SQLException e) {
            mostrarErroSql("Restaurar valor",
                    "Não foi possível restaurar o valor automático.", e);
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Restaurar valor", e.getMessage());
        }
    }

    @FXML
    private void deletarServico() {
        Integer id = lerIdValido("Excluir");

        if (id == null) {
            return;
        }

        try {
            Servico servico = servicoDAO.buscarPorId(id);

            if (servico == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Excluir",
                        "Serviço não encontrado.");
                return;
            }

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Excluir serviço");
            confirmacao.setHeaderText("Confirmar exclusão");
            confirmacao.setContentText(
                    "Deseja realmente excluir o serviço "
                    + servico.getNome() + " (ID: " + id + ")?"
            );

            boolean confirmou = confirmacao.showAndWait()
                    .filter(ButtonType.OK::equals)
                    .isPresent();

            if (!confirmou) {
                return;
            }

            boolean excluido = servicoDAO.deletarPorId(id);

            if (!excluido) {
                mostrarAlerta(Alert.AlertType.WARNING, "Excluir",
                        "Nenhum serviço foi excluído.");
                return;
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Excluir",
                    "Serviço excluído com sucesso!");

            limparCampos();
            carregarServicos();

        } catch (SQLException e) {
            mostrarErroSql("Excluir",
                    "Não foi possível excluir o serviço.", e);
        }
    }


    @FXML
    private void acessarPrestacoes() {
        Servico servico = tabelaServicos
                .getSelectionModel()
                .getSelectedItem();

        if (servico == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Prestações",
                    "Selecione um serviço na tabela.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/mecanicacassoni/view/Prestacao.fxml"
                    )
            );

            Parent root = loader.load();

            PrestacaoController controller = loader.getController();
            controller.definirServico(servico);

            Stage stage = new Stage();
            stage.setTitle("Prestações - " + servico.getNome());
            stage.setScene(new Scene(root));

            stage.setOnHidden(event -> carregarServicos());

            stage.show();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Prestações",
                    "Não foi possível abrir a tela de prestações.");
            e.printStackTrace();
        }
    }


    private void carregarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listarTodos();

            nomesClientes.clear();

            for (Cliente cliente : clientes) {
                nomesClientes.put(
                        cliente.getIdCliente(),
                        cliente.getNome()
                );
            }

            comboClientes.setItems(
                    FXCollections.observableArrayList(clientes)
            );

            tabelaServicos.refresh();

        } catch (SQLException e) {
            mostrarErroSql("Clientes",
                    "Não foi possível carregar os clientes.", e);
        }
    }

    private void carregarTotaisAutomaticos() throws SQLException {
        List<Prestacao> prestacoes = prestacaoDAO.listarTodos();

        Map<Integer, BigDecimal> novosTotais = new HashMap<>();

        for (Prestacao prestacao : prestacoes) {
            BigDecimal valor = calculadora.calcularTotalPrestacao(prestacao);

            novosTotais.merge(
                    prestacao.getIdServico(),
                    valor,
                    BigDecimal::add
            );
        }

        totaisAutomaticos.clear();
        totaisAutomaticos.putAll(novosTotais);
    }

    private void carregarServicos() {
        try {
            carregarTotaisAutomaticos();

            tabelaServicos.setItems(
                    FXCollections.observableArrayList(
                            servicoDAO.listarTodos()
                    )
            );

            tabelaServicos.refresh();

        } catch (SQLException e) {
            mostrarErroSql("Listagem",
                    "Não foi possível carregar os serviços.", e);
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Listagem", e.getMessage());
        }
    }

    private BigDecimal obterValorEfetivo(Servico servico) {
        if (servico.getValorFinal() != null) {
            return servico.getValorFinal();
        }

        return totaisAutomaticos.getOrDefault(
                servico.getIdServico(),
                BigDecimal.ZERO.setScale(2)
        );
    }

    private void preencherCampos(Servico servico) {
        txtId.setText(String.valueOf(servico.getIdServico()));
        txtNome.setText(servico.getNome());

        comboClientes.getSelectionModel().clearSelection();

        for (Cliente cliente : comboClientes.getItems()) {
            if (cliente.getIdCliente().equals(servico.getIdCliente())) {
                comboClientes.setValue(cliente);
                break;
            }
        }

        idServicoExibido = servico.getIdServico();
        valorExibidoOriginal = obterValorEfetivo(servico);

        txtValor.setText(formatarNumero(valorExibidoOriginal));
    }

    private Integer lerIdValido(String operacao) {
        try {
            int id = Integer.parseInt(txtId.getText().trim());

            if (id <= 0) {
                mostrarAlerta(Alert.AlertType.ERROR, operacao,
                        "O ID deve ser maior que zero.");
                return null;
            }

            return id;

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, operacao,
                    "Digite um ID numérico inteiro válido.");
            return null;
        }
    }

    private BigDecimal lerValorMonetario(String operacao) {
        String texto = txtValor.getText().trim();

        if (!texto.matches("\\d+([,.]\\d{1,2})?")) {
            mostrarAlerta(Alert.AlertType.ERROR, operacao,
                    "Digite um valor válido, como 350,00.");
            return null;
        }

        try {
            BigDecimal valor = new BigDecimal(
                    texto.replace(',', '.')
            );

            if (valor.signum() < 0
                    || valor.compareTo(new BigDecimal("99999999.99")) > 0) {
                mostrarAlerta(Alert.AlertType.ERROR, operacao,
                        "O valor deve estar entre 0 e 99.999.999,99.");
                return null;
            }

            return valor.setScale(2);

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, operacao,
                    "O valor informado não é válido.");
            return null;
        }
    }

    private boolean validarNome(String nome, String operacao) {
        if (nome.isEmpty() || nome.length() > 100) {
            mostrarAlerta(Alert.AlertType.ERROR, operacao,
                    "O nome do serviço deve conter entre 1 e 100 caracteres.");
            return false;
        }

        return true;
    }

    private boolean validarCliente(Cliente cliente, String operacao) {
        if (cliente == null) {
            mostrarAlerta(Alert.AlertType.ERROR, operacao,
                    "Selecione um cliente.");
            return false;
        }

        return true;
    }

    private void limparCampos() {
        tabelaServicos.getSelectionModel().clearSelection();

        txtId.clear();
        txtNome.clear();
        txtValor.clear();

        comboClientes.getSelectionModel().clearSelection();

        idServicoExibido = null;
        valorExibidoOriginal = null;
    }

    private String formatarNumero(BigDecimal valor) {
        return valor.setScale(2).toPlainString().replace('.', ',');
    }

    private String formatarMoeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("pt-BR")
        ).format(valor);
    }

    private void mostrarAlerta(Alert.AlertType tipo,
                               String titulo,
                               String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErroSql(String operacao,
                                String mensagem,
                                SQLException e) {
        if (e.getErrorCode() == 1451) {
            mostrarAlerta(Alert.AlertType.WARNING, operacao,
                    "Este serviço possui prestações associadas "
                    + "e não pode ser excluído.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, operacao, mensagem);
        }

        e.printStackTrace();
    }

    @FXML
    private void voltarMenu(ActionEvent event) {
        try {
            Navegacao.trocarTela(event, "Menu.fxml");
        } catch (IOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Navegação");
            alerta.setHeaderText(null);
            alerta.setContentText("Não foi possível voltar ao menu.");
            alerta.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void acessarFiltro() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/mecanicacassoni/view/FiltroServico.fxml"
                    )
            );

            Parent root = loader.load();

            FiltroServicoController controller = loader.getController();
            controller.definirPesquisa(this::aplicarFiltro);

            Stage stage = new Stage();
            stage.setTitle("Filtrar Serviços");
            stage.initOwner(tabelaServicos.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Filtros",
                    "Não foi possível abrir a tela de filtros."
            );
            e.printStackTrace();
        }
    }

    private boolean aplicarFiltro(int idCliente, int ano) {
        try {
            List<Servico> resultado =
                    servicoDAO.listarPorAnoECliente(idCliente, ano);

            carregarTotaisAutomaticos();
            limparCampos();

            tabelaServicos.setItems(
                    FXCollections.observableArrayList(resultado)
            );

            tabelaServicos.refresh();
            return true;

        } catch (SQLException e) {
            mostrarErroSql(
                    "Filtros",
                    "Não foi possível realizar a pesquisa.",
                    e
            );
            return false;

        } catch (IllegalArgumentException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Filtros",
                    e.getMessage()
            );
            return false;
        }
    }

    @FXML
    private void limparFiltro() {
        limparCampos();
        carregarServicos();
    }
}
