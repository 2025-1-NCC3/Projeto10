package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private EditText inputNome, inputSenha;
    private Button btnLogin;
    private TextView cadastroTextView;

    private static final String URL = "https://ubergirls-grupo10-cdbrcyhgg0hjg8dw.brazilsouth-01.azurewebsites.net/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        inputNome = findViewById(R.id.textInputEditText);
        inputSenha = findViewById(R.id.textInputEditText2);
        btnLogin = findViewById(R.id.btnLogin);
        cadastroTextView = findViewById(R.id.TextCadastro);

        btnLogin.setOnClickListener(v -> {
            String nome = inputNome.getText().toString().trim();
            String senha = inputSenha.getText().toString().trim();

            if (nome.isEmpty() || senha.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            fazerLogin(nome, senha);
        });

        cadastroTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    private void fazerLogin(String nome, String senha) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("nome", nome);
            jsonBody.put("senha", senha);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    jsonBody,
                    response -> {
                        Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, TelaHomeActivity.class);
                        startActivity(intent);
                        finish();
                    },
                    error -> {
                        Toast.makeText(LoginActivity.this, "Erro ao fazer login: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
