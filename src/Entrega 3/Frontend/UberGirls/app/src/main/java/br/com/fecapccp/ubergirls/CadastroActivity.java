package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import br.com.fecapccp.ubergirls.util.Constants;

import androidx.appcompat.app.AppCompatActivity;

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
            inputSexo,
            inputSenha;
    private Button btnEscolha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro_main);

        inputNome      = findViewById(R.id.inputNome);
        inputTelefone  = findViewById(R.id.inputTelefone);
        inputEmail     = findViewById(R.id.inputEmail);
        inputCPF       = findViewById(R.id.inputCPF);
        inputSexo      = findViewById(R.id.inputSexo); // trocar por RadioGroup depois
        inputSenha     = findViewById(R.id.inputSenha);
        btnEscolha     = findViewById(R.id.btnEscolha);

        btnEscolha.setOnClickListener(v -> {
            String nome  = inputNome.getText().toString().trim();
            String tel   = inputTelefone.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String cpf   = inputCPF.getText().toString().trim();
            String sexo  = inputSexo.getText().toString().trim().toLowerCase();
            String senha = inputSenha.getText().toString().trim();
            boolean aceitaMotoristaMulher = false; // atualizar depois com CheckBox

            if (nome.isEmpty() || tel.isEmpty() || email.isEmpty() ||
                    cpf.isEmpty()  || sexo.isEmpty()|| senha.isEmpty()) {
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
                body.put("sexo", sexo);
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

    // Se ainda for usar, só atualize a URL aqui também:
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
