package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class PesquisaActivity extends AppCompatActivity {

    private static final int AUTOCOMPLETE_REQUEST_CODE_ORIGEM = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE_DESTINO = 2;

    private EditText editOrigem, editDestino;
    private ImageView btnVoltar, btnSeta;
    private Button btnConcluido;

    private String enderecoOrigemSelecionado = null;
    private String enderecoDestinoSelecionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pesquisa_main);

        // Inicializa o Places API (com sua chave)
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),
                    "AIzaSyD6OQbHAg8n3-qHusRR-KCv6Ii1CiYhjVI");
        }

        // Referências
        editOrigem    = findViewById(R.id.editOrigem);
        editDestino   = findViewById(R.id.editDestino);
        btnVoltar     = findViewById(R.id.btnVoltar);
        btnSeta       = findViewById(R.id.btnSeta);
        btnConcluido  = findViewById(R.id.btnConcluido);

        // Navega para TelaHomeActivity
        btnVoltar.setOnClickListener(view -> {
            Intent intent = new Intent(PesquisaActivity.this, TelaHomeActivity.class);
            startActivity(intent);
        });

        // Clique no campo de origem (abre o autocomplete)
        editOrigem.setFocusable(false); // Impede que o teclado abra automaticamente
        editOrigem.setOnClickListener(view -> {
            try {
                // Define os campos que queremos recuperar
                List<Place.Field> campos = Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                );

                // Cria a intent do autocomplete
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, campos)
                        .setCountries(Arrays.asList("BR")) // Define o país como Brasil
                        .build(PesquisaActivity.this);

                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_ORIGEM);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(PesquisaActivity.this,
                        "Erro ao abrir o autocomplete: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Clique no campo de destino (abre o autocomplete)
        editDestino.setFocusable(false); // Impede que o teclado abra automaticamente
        editDestino.setOnClickListener(view -> {
            try {
                // Define os campos que queremos recuperar
                List<Place.Field> campos = Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                );

                // Cria a intent do autocomplete
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, campos)
                        .setCountries(Arrays.asList("BR")) // Define o país como Brasil
                        .build(PesquisaActivity.this);

                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DESTINO);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(PesquisaActivity.this,
                        "Erro ao abrir o autocomplete: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Apenas o botão concluído fará a navegação para a próxima tela
        btnConcluido.setOnClickListener(view -> {
            String origem = editOrigem.getText().toString();
            String destino = editDestino.getText().toString();

            // Verifica se temos origem
            if (origem.isEmpty()) {
                if (enderecoOrigemSelecionado == null) {
                    Toast.makeText(PesquisaActivity.this, "Informe o local de origem", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    origem = enderecoOrigemSelecionado;
                }
            }

            // Verifica se temos destino
            if (destino.isEmpty()) {
                if (enderecoDestinoSelecionado == null) {
                    Toast.makeText(PesquisaActivity.this, "Selecione um endereço de destino",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    destino = enderecoDestinoSelecionado;
                }
            }

            // Enviar para UberGirlsActivity com os parâmetros de origem e destino
            Intent intent = new Intent(PesquisaActivity.this, UberGirlsActivity.class);
            intent.putExtra("origem", origem);
            intent.putExtra("destino", destino);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                if (requestCode == AUTOCOMPLETE_REQUEST_CODE_ORIGEM) {
                    enderecoOrigemSelecionado = place.getAddress();
                    editOrigem.setText(enderecoOrigemSelecionado);
                } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DESTINO) {
                    enderecoDestinoSelecionado = place.getAddress();
                    editDestino.setText(enderecoDestinoSelecionado);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
                // Handle error
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Erro: " + status.getStatusMessage(),
                        Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // O usuário cancelou a pesquisa
                Toast.makeText(this, "Busca cancelada pelo usuário",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao obter o endereço: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}