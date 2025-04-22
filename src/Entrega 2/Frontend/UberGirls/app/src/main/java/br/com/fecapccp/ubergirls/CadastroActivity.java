package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class CadastroActivity extends AppCompatActivity {

    private EditText inputNome, inputTelefone, inputEmail, inputCPF, inputSexo, inputSenha;
    private Button btnEscolha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro_main);

        inputNome = findViewById(R.id.inputNome);
        inputTelefone = findViewById(R.id.inputTelefone);
        inputEmail = findViewById(R.id.inputEmail);
        inputCPF = findViewById(R.id.inputCPF);
        inputSexo = findViewById(R.id.inputSexo); // vamos trocar por Radio depois
        inputSenha = findViewById(R.id.inputSenha);
        btnEscolha = findViewById(R.id.btnEscolha);

        btnEscolha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = inputNome.getText().toString().trim();
                String telefone = inputTelefone.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String cpf = inputCPF.getText().toString().trim();
                String sexo = inputSexo.getText().toString().trim().toLowerCase();
                String senha = inputSenha.getText().toString().trim();
                boolean aceitaMotoristaMulher = false; // depois integramos com checkbox

                if (nome.isEmpty() || telefone.isEmpty() || email.isEmpty() || cpf.isEmpty() || sexo.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(CadastroActivity.this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                enviarDadosParaServidor(nome, senha, sexo, aceitaMotoristaMulher);

                String url = "https://ubergirls-grupo10-cdbrcyhgg0hjg8dw.brazilsouth-01.azurewebsites.net/cadastro";

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("nome", nome);
                    jsonBody.put("telefone", telefone);
                    jsonBody.put("email", email);
                    jsonBody.put("cpf", cpf);
                    jsonBody.put("senha", senha);
                    jsonBody.put("sexo", sexo);
                    jsonBody.put("aceita_motorista_mulher", aceitaMotoristaMulher ? 1 : 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        response -> {
                            Toast.makeText(CadastroActivity.this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CadastroActivity.this, TelaHomeActivity.class);
                            startActivity(intent);
                            finish();
                        },
                        error -> {
                            Toast.makeText(CadastroActivity.this, "Erro ao cadastrar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                );

                RequestQueue queue = Volley.newRequestQueue(CadastroActivity.this);
                queue.add(request);
            }
        });
    }

    private void enviarDadosParaServidor(String nome, String senha, String sexo, boolean aceitaMotoristaMulher) {
        String url = "https://ubergirls-grupo10-cdbrcyhgg0hjg8dw.brazilsouth-01.azurewebsites.net/cadastro";

        RequestQueue queue = Volley.newRequestQueue(CadastroActivity.this);

        JSONObject dados = new JSONObject();
        try {
            dados.put("nome", nome);
            dados.put("senha", senha);
            dados.put("sexo", sexo);
            dados.put("aceita_motorista_mulher", aceitaMotoristaMulher ? 1 : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest requisicao = new JsonObjectRequest(Request.Method.POST, url, dados,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CadastroActivity.this, TelaHomeActivity.class));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CadastroActivity.this, "Erro ao cadastrar. Tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(requisicao);
    }

}
