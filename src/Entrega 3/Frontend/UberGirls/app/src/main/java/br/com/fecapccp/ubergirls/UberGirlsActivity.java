package br.com.fecapccp.ubergirls;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class UberGirlsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ImageView imgUberGirls, imgUberX, imgComfort;
    private TextView textUberGirls, textUberX, textComfort;
    private Button btnEscolha;
    private ImageView btnVoltar;

    private String opcaoSelecionada = "UberGirls";

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ubergirls_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa o mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MAP_DEBUG", "Erro: fragmento do mapa não encontrado!");
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        imgUberGirls = findViewById(R.id.imgUberGirls);
        imgUberX      = findViewById(R.id.imgUberX);
        imgComfort    = findViewById(R.id.imgComfort);

        textUberGirls = findViewById(R.id.textUberGirls);
        textUberX      = findViewById(R.id.textUberX);
        textComfort    = findViewById(R.id.textComfort);

        btnEscolha = findViewById(R.id.btnEscolha);
        btnVoltar  = findViewById(R.id.btnVoltar);

        imgUberX.setOnClickListener(v -> trocarComPrincipal(imgUberX, textUberX));
        textUberX.setOnClickListener(v -> trocarComPrincipal(imgUberX, textUberX));

        imgComfort.setOnClickListener(v -> trocarComPrincipal(imgComfort, textComfort));
        textComfort.setOnClickListener(v -> trocarComPrincipal(imgComfort, textComfort));

        atualizarBotao();

        // Navega para EscolhaRota
        btnEscolha.setOnClickListener(v -> {
            Intent intent = new Intent(UberGirlsActivity.this, EscolhaRota.class);
            startActivity(intent);
        });

        // Navega para PesquisaMainActivity
        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(UberGirlsActivity.this, PesquisaActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d("MAP_DEBUG", "Mapa carregado com sucesso!");
        mMap.setTrafficEnabled(true);

        // Verifica permissão de localização
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            ativarLocalizacao();
        }
    }

    // Ativa o botão azul de localização e centraliza o mapa
    private void ativarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng localAtual = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localAtual, 15));
                        } else {
                            Log.e("MAP_DEBUG", "Localização atual indisponível.");
                        }
                    });
        }
    }

    // Recebe o resultado da permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ativarLocalizacao();
            } else {
                Log.e("MAP_DEBUG", "Permissão de localização negada.");
            }
        }
    }

    // Troca a imagem/texto principal com a opção clicada
    private void trocarComPrincipal(ImageView imagemSecundaria, TextView textoSecundario) {
        if (textoSecundario.getText().equals(textUberGirls.getText())) return;

        CharSequence textoPrincipal        = textUberGirls.getText();
        CharSequence textoSecundarioAtual = textoSecundario.getText();

        textUberGirls.setText(textoSecundarioAtual);
        textoSecundario.setText(textoPrincipal);

        int imgPrincipalId   = getImagemPorTexto(textoPrincipal.toString());
        int imgSecundariaId = getImagemPorTexto(textoSecundarioAtual.toString());

        imgUberGirls.setImageResource(imgSecundariaId);
        imagemSecundaria.setImageResource(imgPrincipalId);

        opcaoSelecionada = textoSecundarioAtual.toString();
        atualizarBotao();
    }

    // Atualiza o botão inferior com o nome da opção atual
    private void atualizarBotao() {
        btnEscolha.setText("Escolha " + opcaoSelecionada);
    }

    // Retorna o drawable correspondente ao nome da opção
    private int getImagemPorTexto(String nome) {
        switch (nome) {
            case "UberGirls":
                return R.drawable.carro_girls;
            case "UberX":
                return R.drawable.carro_x;
            case "Uber Comfort":
                return R.drawable.carro_comfort;
            default:
                return R.drawable.carro_girls;
        }
    }
}
