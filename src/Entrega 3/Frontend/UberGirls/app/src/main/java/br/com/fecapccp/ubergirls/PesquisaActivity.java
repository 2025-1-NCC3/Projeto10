package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class PesquisaActivity extends AppCompatActivity {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    private EditText editOrigem, editDestino;
    private ImageView btnVoltar, btnSeta;
    private Button btnConcluido;

    private String enderecoSelecionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pesquisa_main);

        // Inicializa o Places API (com sua chave)
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),
                    "AIzaSyD6OQbHAg8n3-qHusRR-KCv61iICiYhjVI");
        }

        // Referências
        editOrigem = findViewById(R.id.editOrigem);
        editDestino = findViewById(R.id.editDestino);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnSeta = findViewById(R.id.btnSeta);
        btnConcluido = findViewById(R.id.btnConcluido);

        // Voltar para a tela anterior
        btnVoltar.setOnClickListener(view -> finish());

        // Clique no campo de destino (abre o autocomplete)
        editDestino.setOnClickListener(view -> {
            List<Place.Field> campos = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
            );

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, campos)
                    .build(PesquisaActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        // Botão seta para ir para UberGirlsActivity
        btnSeta.setOnClickListener(view -> {
            String origem = editOrigem.getText().toString();
            String destino = editDestino.getText().toString();

            if (origem.isEmpty() || destino.isEmpty()) {
                Toast.makeText(this, "Preencha os dois campos", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(PesquisaActivity.this, UberGirlsActivity.class);
                intent.putExtra("origem", origem);
                intent.putExtra("destino", destino);
                startActivity(intent);
            }
        });

        // Botão "Concluído" leva para tela item_local
        btnConcluido.setOnClickListener(view -> {
            if (enderecoSelecionado != null) {
                Intent intent = new Intent(PesquisaActivity.this, ItemLocalActivity.class);
                intent.putExtra("endereco", enderecoSelecionado);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Selecione um endereço no campo 'Para onde?'", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                enderecoSelecionado = place.getAddress();
                editDestino.setText(enderecoSelecionado);
            } else {
                Toast.makeText(this, "Nenhum endereço selecionado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
