package br.com.fecapccp.ubergirls;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UberGirlsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ImageView imgUberGirls, imgUberX, imgComfort;
    private TextView textUberGirls, textUberX, textComfort;
    private Button btnEscolha;
    private ImageView btnVoltar;

    // referências dos TextView de horário
    private TextView textTempoPrincipal;
    private TextView textTempoSec;
    private TextView textTempoTer;

    private String opcaoSelecionada = "UberGirls";

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // atualiza horário a cada minuto
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable atualizarHorarioRunnable = new Runnable() {
        @Override
        public void run() {
            atualizarHorarios();
            handler.postDelayed(this, 120_000);
        }
    };

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

        // encontra os TextView de horário
        textTempoPrincipal = findViewById(R.id.textTempoPrincipal);
        textTempoSec       = findViewById(R.id.textTempoSec);
        textTempoTer       = findViewById(R.id.textTempoTer);

        btnEscolha = findViewById(R.id.btnEscolha);
        btnVoltar  = findViewById(R.id.btnVoltar);

        imgUberX.setOnClickListener(v -> trocarComPrincipal(imgUberX, textUberX));
        textUberX.setOnClickListener(v -> trocarComPrincipal(imgUberX, textUberX));

        imgComfort.setOnClickListener(v -> trocarComPrincipal(imgComfort, textComfort));
        textComfort.setOnClickListener(v -> trocarComPrincipal(imgComfort, textComfort));

        atualizarBotao();

        btnEscolha.setOnClickListener(v -> {
            Intent intent = new Intent(UberGirlsActivity.this, EscolhaRota.class);
            startActivity(intent);
        });

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(UberGirlsActivity.this, PesquisaActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(atualizarHorarioRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(atualizarHorarioRunnable);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d("MAP_DEBUG", "Mapa carregado com sucesso!");
        mMap.setTrafficEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            ativarLocalizacao();
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();
        } else {
            Log.e("MAP_DEBUG", "Permissão de localização negada.");
        }
    }

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

    private void atualizarBotao() {
        btnEscolha.setText("Escolha " + opcaoSelecionada);
    }

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

    // formata e atualiza o horário nos três TextView
    private void atualizarHorarios() {
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());
        String sufixo = " - 3 min de distância";

        textTempoPrincipal.setText(hora + sufixo);
        textTempoSec.setText(hora + sufixo);
        textTempoTer.setText(hora + sufixo);
    }
}
