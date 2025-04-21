package br.com.fecapccp.ubergirls;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        inputSexo = findViewById(R.id.inputSexo);
        inputSenha = findViewById(R.id.inputSenha);


        btnEscolha = findViewById(R.id.btnEscolha);


        btnEscolha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome = inputNome.getText().toString();
                String telefone = inputTelefone.getText().toString();
                String email = inputEmail.getText().toString();
                String cpf = inputCPF.getText().toString();
                String sexo = inputSexo.getText().toString();
                String senha = inputSenha.getText().toString();


                if (nome.isEmpty() || telefone.isEmpty() || email.isEmpty() || cpf.isEmpty() ||
                        sexo.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(CadastroActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                } else {

                    Intent intent = new Intent(CadastroActivity.this, TelaHomeActivity.class);


                    intent.putExtra("nome", nome);
                    intent.putExtra("telefone", telefone);
                    intent.putExtra("email", email);
                    intent.putExtra("cpf", cpf);
                    intent.putExtra("sexo", sexo);
                    intent.putExtra("senha", senha);


                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}