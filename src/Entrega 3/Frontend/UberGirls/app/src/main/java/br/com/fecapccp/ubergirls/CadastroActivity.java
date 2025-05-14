package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import br.com.fecapccp.ubergirls.util.Constants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class CadastroActivity extends AppCompatActivity {

    private EditText inputNome,
            inputTelefone,
            inputEmail,
            inputCPF,
            inputSenha;
    private TextView textViewSexo;
    private Spinner spinnerSexo;
    private Button btnEscolha;
    private String sexoSelecionado = null;
    private String[] opcoesExibicao = {"Sexo", "Feminino", "Masculino"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro_main);

        inputNome      = findViewById(R.id.inputNome);
        inputTelefone  = findViewById(R.id.inputTelefone);
        inputEmail     = findViewById(R.id.inputEmail);
        inputCPF       = findViewById(R.id.inputCPF);
        textViewSexo   = findViewById(R.id.textViewSexo);
        spinnerSexo    = findViewById(R.id.spinnerSexo);
        inputSenha     = findViewById(R.id.inputSenha);
        btnEscolha     = findViewById(R.id.btnEscolha);

        // Configurar o Spinner e TextView
        configurarSpinnerSexo();

        // Configurar o evento de clique no TextView
        textViewSexo.setOnClickListener(v -> {
            // Mostrar o diálogo de seleção quando o TextView for clicado
            showSexoSelectionDialog();
        });

        btnEscolha.setOnClickListener(v -> {
            String nome  = inputNome.getText().toString().trim();
            String tel   = inputTelefone.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String cpf   = inputCPF.getText().toString().trim();
            String senha = inputSenha.getText().toString().trim();
            boolean aceitaMotoristaMulher = false; // atualizar depois com CheckBox

            if (nome.isEmpty() || tel.isEmpty() || email.isEmpty() ||
                    cpf.isEmpty() || sexoSelecionado == null || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Monta o JSON
            JSONObject body = new JSONObject();
            try {
                body.put("nome", nome);
                body.put("telefone", tel);
                body.put("email", email);
                body.put("cpf", cpf);
                body.put("senha", senha);
                body.put("sexo", sexoSelecionado.toLowerCase());
                body.put("aceita_motorista_mulher", aceitaMotoristaMulher ? 1 : 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Usa a URL do Constants
            String url = Constants.BASE_URL + "user";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, TelaHomeActivity.class));
                        finish();
                    },
                    error -> Toast.makeText(
                            this,
                            "Erro ao cadastrar: " + error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show()
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        });
    }

    private void configurarSpinnerSexo() {
        // Criar um ArrayAdapter usando o array de opções
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                opcoesExibicao
        );

        // Aplicar o adapter ao spinner
        spinnerSexo.setAdapter(adapter);

        // Definir o listener para quando um item for selecionado
        spinnerSexo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Ignorar o item "Sexo" (posição 0)
                    sexoSelecionado = opcoesExibicao[position];
                    textViewSexo.setText(sexoSelecionado);
                } else {
                    sexoSelecionado = null;
                    textViewSexo.setText("Sexo");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sexoSelecionado = null;
                textViewSexo.setText("Sexo");
            }
        });
    }

    private void showSexoSelectionDialog() {
        // Mostrar o diálogo de seleção de sexo
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Selecione o sexo");

        // Adicionar os itens ao diálogo
        builder.setItems(new String[]{"Feminino", "Masculino"}, (dialog, which) -> {
            // O índice 'which' começa em 0, então +1 para corresponder ao array opcoesExibicao (que tem "Sexo" na posição 0)
            int selectedIndex = which + 1;
            sexoSelecionado = opcoesExibicao[selectedIndex];
            textViewSexo.setText(sexoSelecionado);
        });

        // Criar e mostrar o diálogo
        builder.create().show();
    }

    // Método existente mantido
    private void enviarDadosParaServidor(String nome, String senha, String sexo, boolean aceitaMotoristaMulher) {
        String url = Constants.BASE_URL + "cadastro";

        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject dados = new JSONObject();
        try {
            dados.put("nome", nome);
            dados.put("senha", senha);
            dados.put("sexo", sexo);
            dados.put("aceita_motorista_mulher", aceitaMotoristaMulher ? 1 : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest requisicao = new JsonObjectRequest(
                Request.Method.POST,
                url,
                dados,
                resp -> Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show(),
                err  -> Toast.makeText(this, "Erro ao cadastrar. Tente novamente.", Toast.LENGTH_SHORT).show()
        );

        queue.add(requisicao);
    }
}