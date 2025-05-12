package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TelaHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.telahome_main);

        // Referência ao TextView "Para onde?"
        TextView tvParaOnde = findViewById(R.id.tvParaOnde);

        // Ação de clique para abrir a tela de pesquisa
        tvParaOnde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelaHomeActivity.this, PesquisaActivity.class);
                startActivity(intent);
            }
        });
    }
}
