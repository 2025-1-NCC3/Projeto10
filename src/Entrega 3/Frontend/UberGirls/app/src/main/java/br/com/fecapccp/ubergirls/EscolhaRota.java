package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Color;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.location.Address;
import android.location.Geocoder;

public class EscolhaRota extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline rotaPolyline;

    // Cores das rotas
    private static final int COR_ROTA_SEGURA = Color.parseColor("#6200EE");  // Roxo escuro
    private static final int COR_ROTA_MEDIA = Color.parseColor("#9C27B0");    // Roxo médio
    private static final int COR_ROTA_PERIGOSA = Color.parseColor("#E91E63"); // Rosa/vermelho

    // Elementos da UI
    private Button btnConfirmarPartida;
    private CardView cardMelhorRota;
    private TextView textHome;
    private ImageView btnVoltar;

    private TextView textPesquisa;
    private TextView textTempoMelhor;
    private TextView textKmMelhor;
    private TextView textRotaMelhor;
    private TextView textInfoMelhor;


    private LatLng origem;
    private LatLng destino;


    private String enderecoOrigem;
    private String enderecoDestino;


    private int rotaSelecionada = 0;
    private double distanciaEstimada = 0;
    private int tempoEstimado = 0;


    private static final String TAG = "EscolhaRota";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escolharota_main);

        // Inicializar elementos da UI
        btnConfirmarPartida = findViewById(R.id.btnConfirmarPartida);
        textPesquisa = findViewById(R.id.textPesquisa);
        cardMelhorRota = findViewById(R.id.cardMelhorRota);
        textHome = findViewById(R.id.textHome);
        btnVoltar = findViewById(R.id.btnVoltar);
        textTempoMelhor = findViewById(R.id.textTempoMelhor);
        textKmMelhor = findViewById(R.id.textKmMelhor);
        textRotaMelhor = findViewById(R.id.textRotaMelhor);
        textInfoMelhor = findViewById(R.id.textInfoMelhor);


        btnVoltar.setOnClickListener(v -> finish());


        obterDadosRotaSelecionada();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        configurarListeners();
    }



    private void obterDadosRotaSelecionada() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            enderecoOrigem = extras.getString("origem", "Endereço de origem");
            enderecoDestino = extras.getString("destino", "Endereço de destino");


            rotaSelecionada = extras.getInt("rotaSelecionada", 0);
            distanciaEstimada = extras.getDouble("distancia", 0);
            tempoEstimado = extras.getInt("tempo", 0);


            textHome.setText(enderecoOrigem);


            geocodificarEnderecos();

            Log.d(TAG, "Rota selecionada: " + rotaSelecionada);
        } else {

            enderecoOrigem = "FECAP, São Paulo";
            enderecoDestino = "Avenida Paulista, São Paulo";
            textHome.setText(enderecoOrigem);


            origem = new LatLng(-23.5505, -46.6333);  // Centro de São Paulo
            destino = new LatLng(-23.5905, -46.6933); // Exemplo de destino
        }
    }

    private void geocodificarEnderecos() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            // Geocodificar o endereço de origem
            List<Address> listaOrigem = geocoder.getFromLocationName(enderecoOrigem, 1);
            if (listaOrigem != null && listaOrigem.size() > 0) {
                Address endereco = listaOrigem.get(0);
                origem = new LatLng(endereco.getLatitude(), endereco.getLongitude());
                Log.d(TAG, "Origem geocodificada: " + origem.toString());
            } else {

                Log.w(TAG, "Não foi possível geocodificar o endereço de origem");
                origem = new LatLng(-23.5505, -46.6333);
            }


            List<Address> listaDestino = geocoder.getFromLocationName(enderecoDestino, 1);
            if (listaDestino != null && listaDestino.size() > 0) {
                Address endereco = listaDestino.get(0);
                destino = new LatLng(endereco.getLatitude(), endereco.getLongitude());
                Log.d(TAG, "Destino geocodificado: " + destino.toString());
            } else {

                Log.w(TAG, "Não foi possível geocodificar o endereço de destino");
                destino = new LatLng(-23.5905, -46.6933);
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao geocodificar endereços", e);
            Toast.makeText(this, "Erro ao obter coordenadas dos endereços", Toast.LENGTH_SHORT).show();


            origem = new LatLng(-23.5505, -46.6333);
            destino = new LatLng(-23.5905, -46.6933);
        }
    }

    private void configurarListeners() {

        btnConfirmarPartida.setOnClickListener(v -> {
            Toast.makeText(EscolhaRota.this, "Partida confirmada!", Toast.LENGTH_SHORT).show();

             Intent intent = new Intent(EscolhaRota.this, CorridaSimulacaoActivity.class);
             startActivity(intent);
        });

        textPesquisa.setOnClickListener(v -> {
            Intent voltarIntent = new Intent(EscolhaRota.this, PesquisaActivity.class);
            startActivity(voltarIntent);
        });


        cardMelhorRota.setOnClickListener(v -> {

            Toast.makeText(EscolhaRota.this, "Detalhes da rota selecionada", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);


        if (origem == null || destino == null) {
            Log.e(TAG, "Coordenadas de origem ou destino são nulas");
            Toast.makeText(this, "Erro ao carregar coordenadas", Toast.LENGTH_SHORT).show();
            return;
        }


        mMap.addMarker(new MarkerOptions()
                .position(origem)
                .title("Origem: " + enderecoOrigem)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Destino: " + enderecoDestino)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


        desenharRotaSelecionada();


        ajustarCamera();


        if (distanciaEstimada > 0 && tempoEstimado > 0) {

            atualizarInformacoesComDadosRecebidos();
        } else {

            calcularTempoDistancia();
        }
    }


    private void desenharRotaSelecionada() {

        if (rotaPolyline != null) {
            rotaPolyline.remove();
        }


        double distancia = Math.sqrt(
                Math.pow(destino.latitude - origem.latitude, 2) +
                        Math.pow(destino.longitude - origem.longitude, 2)
        );


        double fatorBase = distancia / 30;


        double fatorVariacao = fatorBase;
        switch (rotaSelecionada) {
            case 0:
                fatorVariacao = fatorBase;
                break;
            case 1:
                fatorVariacao = fatorBase * 2;
                break;
            case 2:
                fatorVariacao = fatorBase * 3;
                break;
        }


        List<LatLng> pontosRota = criarRotaSimulada(origem, destino, fatorVariacao);


        int corRota;
        switch (rotaSelecionada) {
            case 0:
                corRota = COR_ROTA_SEGURA;
                break;
            case 1:
                corRota = COR_ROTA_MEDIA;
                break;
            case 2:
                corRota = COR_ROTA_PERIGOSA;
                break;
            default:
                corRota = COR_ROTA_SEGURA;
                break;
        }


        rotaPolyline = mMap.addPolyline(new PolylineOptions()
                .addAll(pontosRota)
                .color(corRota)
                .width(12));
    }

    private List<LatLng> criarRotaSimulada(LatLng origem, LatLng destino, double fatorVariacao) {

        List<LatLng> pontos = new ArrayList<>();
        pontos.add(origem);



        double lat = origem.latitude;
        double lng = origem.longitude;
        double latDest = destino.latitude;
        double lngDest = destino.longitude;


        double distancia = Math.sqrt(
                Math.pow(latDest - lat, 2) +
                        Math.pow(lngDest - lng, 2)
        );

        int numPontos = Math.max(5, (int)(distancia * 500)); // Min 5 pontos
        numPontos = Math.min(numPontos, 20);

        double latStep = (latDest - lat) / numPontos;
        double lngStep = (lngDest - lng) / numPontos;

        for (int i = 1; i < numPontos; i++) {

            double latVar = Math.random() * fatorVariacao * (Math.random() > 0.5 ? 1 : -1);
            double lngVar = Math.random() * fatorVariacao * (Math.random() > 0.5 ? 1 : -1);

            pontos.add(new LatLng(
                    lat + (latStep * i) + latVar,
                    lng + (lngStep * i) + lngVar
            ));
        }

        pontos.add(destino);
        return pontos;
    }

    private void ajustarCamera() {
        try {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origem);
            builder.include(destino);


            double latDif = destino.latitude - origem.latitude;
            double lngDif = destino.longitude - origem.longitude;


            if (Math.abs(latDif) > 0.1 || Math.abs(lngDif) > 0.1) {
                builder.include(new LatLng(
                        origem.latitude + latDif/3,
                        origem.longitude + lngDif/3
                ));
                builder.include(new LatLng(
                        origem.latitude + 2 * latDif/3,
                        origem.longitude + 2 * lngDif/3
                ));
            }

            LatLngBounds bounds = builder.build();


            int padding = 100;
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            Log.e(TAG, "Erro ao ajustar câmera", e);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origem, 12));
        }
    }

    private void atualizarInformacoesComDadosRecebidos() {

        String tempoStr = tempoEstimado + " min";

        String distanciaStr = String.format("%.1f km", distanciaEstimada);


        String descricaoRota;
        int corRota;

        switch (rotaSelecionada) {
            case 0:
                descricaoRota = "Melhor rota, trânsito normal";
                corRota = COR_ROTA_SEGURA;
                break;
            case 1:
                descricaoRota = "Rota alternativa, trânsito moderado";
                corRota = COR_ROTA_MEDIA;
                break;
            case 2:
                descricaoRota = "Rota com áreas de atenção, evitar à noite";
                corRota = COR_ROTA_PERIGOSA;
                break;
            default:
                descricaoRota = "Rota selecionada";
                corRota = COR_ROTA_SEGURA;
                break;
        }


        textTempoMelhor.setText(tempoStr);
        textKmMelhor.setText(distanciaStr);
        textRotaMelhor.setText("Via selecionada entre " + enderecoOrigem + " e " + enderecoDestino);
        textInfoMelhor.setText(descricaoRota);
        textInfoMelhor.setTextColor(corRota);
    }

    private void calcularTempoDistancia() {

        float[] resultado = new float[1];
        android.location.Location.distanceBetween(
                origem.latitude, origem.longitude,
                destino.latitude, destino.longitude,
                resultado);

        float distanciaMetros = resultado[0];
        float distanciaKm = distanciaMetros / 1000;


        int tempoMinutos;
        switch (rotaSelecionada) {
            case 0:
                tempoMinutos = Math.round((distanciaKm / 30) * 60);
                break;
            case 1:
                tempoMinutos = Math.round(((distanciaKm / 30) * 60) * 1.2f);
                break;
            case 2:
                tempoMinutos = Math.round(((distanciaKm / 30) * 60) * 1.4f);
                break;
            default:
                tempoMinutos = Math.round((distanciaKm / 30) * 60);
                break;
        }


        tempoMinutos = Math.max(tempoMinutos, 10);


        String descricaoRota;
        int corRota;

        switch (rotaSelecionada) {
            case 0:
                descricaoRota = "Melhor rota, trânsito normal";
                corRota = COR_ROTA_SEGURA;
                break;
            case 1:
                descricaoRota = "Rota alternativa, trânsito moderado";
                corRota = COR_ROTA_MEDIA;
                break;
            case 2:
                descricaoRota = "Rota com áreas de atenção, evitar à noite";
                corRota = COR_ROTA_PERIGOSA;
                break;
            default:
                descricaoRota = "Rota selecionada";
                corRota = COR_ROTA_SEGURA;
                break;
        }

        textTempoMelhor.setText(tempoMinutos + " min");
        textKmMelhor.setText(String.format("%.1f km", distanciaKm));
        textRotaMelhor.setText("Via selecionada entre " + enderecoOrigem + " e " + enderecoDestino);
        textInfoMelhor.setText(descricaoRota);
        textInfoMelhor.setTextColor(corRota);
    }
}