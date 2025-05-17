package br.com.fecapccp.ubergirls;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UberGirlsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ImageView imgUberGirls, imgUberX, imgComfort;
    private TextView textUberGirls, textUberX, textComfort;
    private Button btnEscolha;
    private ImageView btnVoltar;


    private TextView textTempoPrincipal;
    private TextView textTempoSec;
    private TextView textTempoTer;

    private String opcaoSelecionada = "UberGirls";


    private String enderecoOrigem;
    private String enderecoDestino;
    private LatLng pontoOrigem;
    private LatLng pontoDestino;
    private Marker marcadorOrigem;
    private Marker marcadorDestino;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Chave da API do Google
    private static final String API_KEY = "AIzaSyD6OQbHAg8n3-qHusRR-KCv61iICiYhjVI";


    private static final float ZOOM_ORIGEM = 16f; // Aumentado para proporcionar um zoom mais próximo


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


        Intent intent = getIntent();
        if (intent != null) {
            enderecoOrigem = intent.getStringExtra("origem");
            enderecoDestino = intent.getStringExtra("destino");

            if (enderecoOrigem == null || enderecoDestino == null) {
                Toast.makeText(this, "Erro: endereços não fornecidos", Toast.LENGTH_SHORT).show();
                finish(); // Volta para a tela anterior
                return;
            }

            Log.d("ROTA_DEBUG", "Origem: " + enderecoOrigem);
            Log.d("ROTA_DEBUG", "Destino: " + enderecoDestino);
        }

        // Inicializar os componentes da UI
        inicializarComponentes();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MAP_DEBUG", "Erro: fragmento do mapa não encontrado!");
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void inicializarComponentes() {
        imgUberGirls = findViewById(R.id.imgUberGirls);
        imgUberX = findViewById(R.id.imgUberX);
        imgComfort = findViewById(R.id.imgComfort);

        textUberGirls = findViewById(R.id.textUberGirls);
        textUberX = findViewById(R.id.textUberX);
        textComfort = findViewById(R.id.textComfort);


        textTempoPrincipal = findViewById(R.id.textTempoPrincipal);
        textTempoSec = findViewById(R.id.textTempoSec);
        textTempoTer = findViewById(R.id.textTempoTer);

        btnEscolha = findViewById(R.id.btnEscolha);
        btnVoltar = findViewById(R.id.btnVoltar);


        imgUberX.setOnClickListener(v -> trocarComPrincipal(imgUberX, textUberX));
        textUberX.setOnClickListener(v -> trocarComPrincipal(imgUberX, textUberX));

        imgComfort.setOnClickListener(v -> trocarComPrincipal(imgComfort, textComfort));
        textComfort.setOnClickListener(v -> trocarComPrincipal(imgComfort, textComfort));

        atualizarBotao();

        btnEscolha.setOnClickListener(v -> {

            Intent escolhaIntent = new Intent(UberGirlsActivity.this, activity_selecao_rota.class);


            escolhaIntent.putExtra("origem", enderecoOrigem);
            escolhaIntent.putExtra("destino", enderecoDestino);


            startActivity(escolhaIntent);
        });

        btnVoltar.setOnClickListener(v -> {
            Intent voltarIntent = new Intent(UberGirlsActivity.this, PesquisaActivity.class);
            startActivity(voltarIntent);
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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {

            geocodeEnderecos();
        }
    }

    private void geocodeEnderecos() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {

            geocodificarEndereco(geocoder, enderecoOrigem, true);
        } catch (IOException e) {
            Log.e("MAP_DEBUG", "Erro ao geocodificar endereços: " + e.getMessage());
            Toast.makeText(this, "Erro ao processar endereços", Toast.LENGTH_SHORT).show();
        }
    }

    private void geocodificarEndereco(Geocoder geocoder, String endereco, boolean isOrigem) throws IOException {

        List<Address> enderecos = geocoder.getFromLocationName(endereco, 1);

        if (enderecos != null && !enderecos.isEmpty()) {
            Address addr = enderecos.get(0);
            LatLng ponto = new LatLng(addr.getLatitude(), addr.getLongitude());

            if (isOrigem) {
                pontoOrigem = ponto;
                Log.d("GEOCODE_DEBUG", "Origem geocodificada: " + pontoOrigem.latitude + "," + pontoOrigem.longitude);


                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));


                try {
                    geocodificarEndereco(geocoder, enderecoDestino, false);
                } catch (IOException e) {
                    Log.e("MAP_DEBUG", "Erro ao geocodificar destino: " + e.getMessage());
                    Toast.makeText(this, "Erro ao processar endereço de destino", Toast.LENGTH_SHORT).show();
                }
            } else {
                pontoDestino = ponto;
                Log.d("GEOCODE_DEBUG", "Destino geocodificado: " + pontoDestino.latitude + "," + pontoDestino.longitude);


                tracarRota();


                ativarLocalizacao();
            }
        } else {
            String tipo = isOrigem ? "origem" : "destino";
            Toast.makeText(this, "Não foi possível encontrar o endereço de " + tipo, Toast.LENGTH_SHORT).show();

            if (isOrigem) {

                obterLocalizacaoAtualComoOrigem();
            }
        }
    }

    private void obterLocalizacaoAtualComoOrigem() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                pontoOrigem = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("GEOCODE_DEBUG", "Usando localização atual como origem: " +
                        pontoOrigem.latitude + "," + pontoOrigem.longitude);


                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));


                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    geocodificarEndereco(geocoder, enderecoDestino, false);
                } catch (IOException e) {
                    Log.e("MAP_DEBUG", "Erro ao geocodificar destino: " + e.getMessage());
                }
            } else {
                Toast.makeText(this, "Não foi possível obter sua localização atual",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tracarRota() {
        if (pontoOrigem == null || pontoDestino == null) {
            Log.e("MAP_DEBUG", "Pontos de origem ou destino são nulos");
            return;
        }


        if (marcadorOrigem != null) marcadorOrigem.remove();
        if (marcadorDestino != null) marcadorDestino.remove();


        marcadorOrigem = mMap.addMarker(new MarkerOptions()
                .position(pontoOrigem)
                .title("Origem: " + enderecoOrigem)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        marcadorDestino = mMap.addMarker(new MarkerOptions()
                .position(pontoDestino)
                .title("Destino: " + enderecoDestino)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));


        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(API_KEY)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        try {
            // Obter direções usando a API
            com.google.maps.model.LatLng origem = new com.google.maps.model.LatLng(
                    pontoOrigem.latitude, pontoOrigem.longitude);
            com.google.maps.model.LatLng destino = new com.google.maps.model.LatLng(
                    pontoDestino.latitude, pontoDestino.longitude);


            Log.d("DIRECTIONS_DEBUG", "Solicitando rota de " + origem + " para " + destino);


            DirectionsResult resultado = DirectionsApi.newRequest(context)
                    .mode(TravelMode.DRIVING)  // Modo veicular
                    .origin(origem)
                    .destination(destino)
                    .optimizeWaypoints(true)   // Otimizar waypoints
                    .await();


            processarResultadoDirecoes(resultado);

        } catch (Exception e) {
            Log.e("MAP_DEBUG", "Erro ao traçar rota: " + e.getMessage());
            e.printStackTrace();

            Toast.makeText(this, "Erro ao obter a rota. Mostrando rota alternativa.",
                    Toast.LENGTH_SHORT).show();

            mostrarRotaAlternativa();
        }
    }

    private void processarResultadoDirecoes(DirectionsResult resultado) {
        runOnUiThread(() -> {
            if (resultado.routes != null && resultado.routes.length > 0) {
                DirectionsRoute rota = resultado.routes[0];

                Log.d("DIRECTIONS_DEBUG", "Rota encontrada com sucesso!");


                long duracaoSegundos = 0;
                float distanciaMetros = 0;

                if (rota.legs != null && rota.legs.length > 0) {
                    duracaoSegundos = rota.legs[0].duration.inSeconds;
                    distanciaMetros = rota.legs[0].distance.inMeters;

                    Log.d("DIRECTIONS_DEBUG", "Duração: " + duracaoSegundos + "s, Distância: " + distanciaMetros + "m");
                }


                atualizarInfoRota(duracaoSegundos, distanciaMetros);


                List<LatLng> pontos = PolyUtil.decode(rota.overviewPolyline.getEncodedPath());


                mMap.clear();


                marcadorOrigem = mMap.addMarker(new MarkerOptions()
                        .position(pontoOrigem)
                        .title("Origem: " + enderecoOrigem)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                marcadorDestino = mMap.addMarker(new MarkerOptions()
                        .position(pontoDestino)
                        .title("Destino: " + enderecoDestino)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


                PolylineOptions linhaDaRota = new PolylineOptions()
                        .addAll(pontos)
                        .width(12)  // Linha um pouco mais grossa
                        .color(Color.parseColor("#4285F4"))  // Azul do Google Maps
                        .geodesic(true);  // Seguir a curvatura da Terra

                mMap.addPolyline(linhaDaRota);


                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));


                new Handler(Looper.getMainLooper()).postDelayed(() -> {

                    if (pontos.size() > 1) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();


                        for (LatLng ponto : pontos) {
                            builder.include(ponto);
                        }

                        LatLngBounds limites = builder.build();
                        int padding = 200;


                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(limites, padding));
                    }
                }, 3000);
            } else {
                Log.e("DIRECTIONS_DEBUG", "Nenhuma rota encontrada no resultado");
                mostrarRotaAlternativa();
            }
        });
    }

    private void mostrarRotaAlternativa() {

        mMap.clear();


        marcadorOrigem = mMap.addMarker(new MarkerOptions()
                .position(pontoOrigem)
                .title("Origem: " + enderecoOrigem)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        marcadorDestino = mMap.addMarker(new MarkerOptions()
                .position(pontoDestino)
                .title("Destino: " + enderecoDestino)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


        PolylineOptions linhaDireta = new PolylineOptions()
                .add(pontoOrigem)
                .add(pontoDestino)
                .width(10)
                .color(Color.RED)
                .geodesic(true);  // Para seguir a curvatura da Terra

        mMap.addPolyline(linhaDireta);


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));


        estimarInfoRotaSimples();


        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pontoOrigem);
            builder.include(pontoDestino);
            LatLngBounds limites = builder.build();

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(limites, 200));
        }, 3000);  // 3 segundos
    }

    private void estimarInfoRotaSimples() {

        float[] results = new float[1];
        Location.distanceBetween(
                pontoOrigem.latitude, pontoOrigem.longitude,
                pontoDestino.latitude, pontoDestino.longitude,
                results);

        float distanciaMetros = results[0];


        float distanciaAjustada = distanciaMetros * 1.3f;
        long duracaoSegundos = (long) (distanciaAjustada / (30 * 1000 / 3600));


        atualizarInfoRota(duracaoSegundos, distanciaAjustada);
    }

    private void atualizarInfoRota(long duracaoSegundos, float distanciaMetros) {

        int minutos = (int) (duracaoSegundos / 60);


        float kmDistancia = distanciaMetros / 1000;


        String duracao = minutos + " min";
        String distancia = String.format(Locale.getDefault(), "%.1f km", kmDistancia);


        String infoCompleta = duracao + " - " + distancia + " de distância";
        textTempoPrincipal.setText(infoCompleta);
        textTempoSec.setText(infoCompleta);
        textTempoTer.setText(infoCompleta);
    }

    private void ativarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();


            if (pontoOrigem == null) {
                obterLocalizacaoAtualComoOrigem();
            }
        } else {
            Log.e("MAP_DEBUG", "Permissão de localização negada.");


            geocodeEnderecos();
        }
    }

    private void trocarComPrincipal(ImageView imagemSecundaria, TextView textoSecundario) {
        if (textoSecundario.getText().equals(textUberGirls.getText())) return;

        CharSequence textoPrincipal = textUberGirls.getText();
        CharSequence textoSecundarioAtual = textoSecundario.getText();

        textUberGirls.setText(textoSecundarioAtual);
        textoSecundario.setText(textoPrincipal);

        int imgPrincipalId = getImagemPorTexto(textoPrincipal.toString());
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


    private void atualizarHorarios() {
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());
        String sufixo = " - tempo estimado";


        if (pontoOrigem == null || pontoDestino == null) {
            textTempoPrincipal.setText(hora + sufixo);
            textTempoSec.setText(hora + sufixo);
            textTempoTer.setText(hora + sufixo);
        }
    }
}