import com.google.gson.Gson;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CepFinder extends JFrame {

    private final JTextField txtCep;
    private final JTextArea txtResultado;

    public CepFinder() {
        setTitle("Busca de CEP");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Componentes da interface
        txtCep = new JTextField();
        JButton btnBuscar = new JButton("Buscar");
        txtResultado = new JTextArea();
        txtResultado.setEditable(false);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("CEP:"), BorderLayout.WEST);
        topPanel.add(txtCep, BorderLayout.CENTER);
        topPanel.add(btnBuscar, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtResultado), BorderLayout.CENTER);

        add(panel);

        // Ação do botão
        btnBuscar.addActionListener(e -> buscarCep());
    }

    private void buscarCep() {
        String cep = txtCep.getText().replaceAll("[^0-9]", "");

        if (cep.length() != 8) {
            JOptionPane.showMessageDialog(this, "CEP inválido! Deve conter 8 dígitos.");
            return;
        }

        try {
            System.out.println("CEP digitado: " + cep); // Log do CEP digitado
            String json = fazerRequisicao(cep);
            System.out.println("Resposta JSON: " + json); // Log da resposta JSON
            Endereco endereco = new Gson().fromJson(json, Endereco.class);

            if (endereco.cep == null) {
                txtResultado.setText("CEP não encontrado");
                return;
            }

            String resultado = String.format(
                "CEP: %s\nLogradouro: %s\nBairro: %s\nCidade: %s\nUF: %s",
                endereco.cep,
                endereco.logradouro,
                endereco.bairro,
                endereco.localidade,
                endereco.uf
            );

            txtResultado.setText(resultado);

        } catch (IOException | InterruptedException ex) {
            JOptionPane.showMessageDialog(this, "Erro na conexão: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao processar a resposta: " + ex.getMessage());
        }
    }

    private String fazerRequisicao(String cep) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://viacep.com.br/ws/" + cep + "/json/"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Erro na requisição: " + response.statusCode());
        }

        return response.body();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CepFinder().setVisible(true));
    }
}

class Endereco {
    String cep;
    String logradouro;
    String bairro;
    String localidade;
    String uf;
}