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

        // Inicializa a API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),
                    "AIzaSyD6OQbHAg8n3-qHusRR-KCv6Ii1CiYhjVI");
        }


        editOrigem    = findViewById(R.id.editOrigem);
        editDestino   = findViewById(R.id.editDestino);
        btnVoltar     = findViewById(R.id.btnVoltar);
        btnSeta       = findViewById(R.id.btnSeta);
        btnConcluido  = findViewById(R.id.btnConcluido);


        btnVoltar.setOnClickListener(view -> {
            Intent intent = new Intent(PesquisaActivity.this, TelaHomeActivity.class);
            startActivity(intent);
        });


        editOrigem.setFocusable(false);
        editOrigem.setOnClickListener(view -> {
            try {

                List<Place.Field> campos = Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                );

                // Cria a intent do autocomplete
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, campos)
                        .setCountries(Arrays.asList("BR"))
                        .build(PesquisaActivity.this);

                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_ORIGEM);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(PesquisaActivity.this,
                        "Erro ao abrir o autocomplete: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });


        editDestino.setFocusable(false);
        editDestino.setOnClickListener(view -> {
            try {

                List<Place.Field> campos = Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                );


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


        btnConcluido.setOnClickListener(view -> {
            String origem = editOrigem.getText().toString();
            String destino = editDestino.getText().toString();


            if (origem.isEmpty()) {
                if (enderecoOrigemSelecionado == null) {
                    Toast.makeText(PesquisaActivity.this, "Informe o local de origem", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    origem = enderecoOrigemSelecionado;
                }
            }

            if (destino.isEmpty()) {
                if (enderecoDestinoSelecionado == null) {
                    Toast.makeText(PesquisaActivity.this, "Selecione um endereço de destino",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    destino = enderecoDestinoSelecionado;
                }
            }

            // Enviar diretamente para UberGirlsActivity com os parâmetros de origem e destino
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