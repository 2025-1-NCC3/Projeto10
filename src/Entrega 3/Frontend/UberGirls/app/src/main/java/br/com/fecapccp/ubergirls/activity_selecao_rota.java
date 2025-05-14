package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class activity_selecao_rota extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "RotaSeguraActivity";

    private GoogleMap mMap;
    private List<Polyline> rotaPolylines = new ArrayList<>();
    private List<List<LatLng>> rotasPontos = new ArrayList<>();


    private static final int COR_ROTA_MELHOR = Color.parseColor("#6200EE");  // Roxo (melhor rota)
    private static final int COR_ROTA_MEDIA = Color.parseColor("#9C27B0");   // Roxo médio
    private static final int COR_ROTA_PIOR = Color.parseColor("#E91E63");    // Rosa (pior)


    private TextView indicatorRota1;
    private TextView indicatorRota2;
    private TextView indicatorRota3;


    private CardView cardRota1;
    private CardView cardRota2;
    private CardView cardRota3;


    private Button btnConfirmarPartida;

    private Marker origemMarker;
    private Marker destinoMarker;


    private LatLng origem;
    private LatLng destino;


    private int selectedRouteIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_selecao_rota);

            Log.d(TAG, "Iniciando RotaSegura Activity");

            inicializarUI();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
                Log.d(TAG, "Requisitando carregamento do mapa");
            } else {
                Log.e(TAG, "MapFragment não encontrado!");
            }

            configurarListeners();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar activity: " + e.getMessage());
            e.printStackTrace();
            // Optional: Show a toast message
            Toast.makeText(this, "Erro ao iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Textos de informações das rotas
    private TextView txtTempoRota1, txtDistanciaRota1, txtViaRota1, txtInfoRota1;
    private TextView txtTempoRota2, txtDistanciaRota2, txtViaRota2, txtInfoRota2, txtAvisoRota2;
    private TextView txtTempoRota3, txtDistanciaRota3, txtViaRota3, txtInfoRota3, txtPolicia, txtAvisoRota3;
    private ImageView imgAvisoRota2, imgPolicia, imgAvisoRota3;

    private void inicializarUI() {

        indicatorRota1 = findViewById(R.id.indicatorRota1);
        indicatorRota2 = findViewById(R.id.indicatorRota2);
        indicatorRota3 = findViewById(R.id.indicatorRota3);


        cardRota1 = findViewById(R.id.cardRota1);
        cardRota2 = findViewById(R.id.cardRota2);
        cardRota3 = findViewById(R.id.cardRota3);


        btnConfirmarPartida = findViewById(R.id.btnConfirmarPartida);


        txtTempoRota1 = findViewById(R.id.txtTempoRota1);
        txtDistanciaRota1 = findViewById(R.id.txtDistanciaRota1);
        txtViaRota1 = findViewById(R.id.txtViaRota1);
        txtInfoRota1 = findViewById(R.id.txtInfoRota1);


        txtTempoRota2 = findViewById(R.id.txtTempoRota2);
        txtDistanciaRota2 = findViewById(R.id.txtDistanciaRota2);
        txtViaRota2 = findViewById(R.id.txtViaRota2);
        txtInfoRota2 = findViewById(R.id.txtInfoRota2);
        txtAvisoRota2 = findViewById(R.id.txtAvisoRota2);
        imgAvisoRota2 = findViewById(R.id.imgAvisoRota2);


        txtTempoRota3 = findViewById(R.id.txtTempoRota3);
        txtDistanciaRota3 = findViewById(R.id.txtDistanciaRota3);
        txtViaRota3 = findViewById(R.id.txtViaRota3);
        txtInfoRota3 = findViewById(R.id.txtInfoRota3);
        txtPolicia = findViewById(R.id.txtPolicia);
        txtAvisoRota3 = findViewById(R.id.txtAvisoRota3);
        imgPolicia = findViewById(R.id.imgPolicia);
        imgAvisoRota3 = findViewById(R.id.imgAvisoRota3);
    }

    private void configurarListeners() {

        cardRota1.setOnClickListener(v -> {
            selecionarRota(0);
            destacarCardSelecionado(cardRota1);
        });

        cardRota2.setOnClickListener(v -> {
            selecionarRota(1);
            destacarCardSelecionado(cardRota2);
        });

        cardRota3.setOnClickListener(v -> {
            selecionarRota(2);
            destacarCardSelecionado(cardRota3);
        });


        indicatorRota1.setOnClickListener(v -> {
            selecionarRota(0);
            destacarCardSelecionado(cardRota1);
        });

        indicatorRota2.setOnClickListener(v -> {
            selecionarRota(1);
            destacarCardSelecionado(cardRota2);
        });

        indicatorRota3.setOnClickListener(v -> {
            selecionarRota(2);
            destacarCardSelecionado(cardRota3);
        });


        btnConfirmarPartida.setOnClickListener(v -> {
            Toast.makeText(activity_selecao_rota.this, "Partida confirmada! Rota " + (selectedRouteIndex + 1) + " selecionada.", Toast.LENGTH_SHORT).show();

        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Mapa carregado com sucesso");

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        try {
            Intent intent = getIntent();
            if (intent != null) {
                double origemLat = intent.getDoubleExtra("ORIGEM_LAT", -23.5505);
                double origemLng = intent.getDoubleExtra("ORIGEM_LNG", -46.6333);
                double destinoLat = intent.getDoubleExtra("DESTINO_LAT", -23.5632);
                double destinoLng = intent.getDoubleExtra("DESTINO_LNG", -46.6546);

                origem = new LatLng(origemLat, origemLng);
                destino = new LatLng(destinoLat, destinoLng);

                Log.d(TAG, "Coordenadas recebidas - Origem: " + origem + " - Destino: " + destino);
            } else {
                origem = new LatLng(-23.5505, -46.6333);
                destino = new LatLng(-23.5632, -46.6546);
                Log.d(TAG, "Usando coordenadas padrão");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter coordenadas: " + e.getMessage());
            origem = new LatLng(-23.5505, -46.6333);
            destino = new LatLng(-23.5632, -46.6546);
        }

        adicionarMarcadores();
        gerarRotasSimuladas();
        desenharRotas();
        selecionarRota(0);
        destacarCardSelecionado(cardRota1);

        // Ajustamos a visualização do mapa após um pequeno delay para garantir que ele esteja renderizado
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        mapView.post(new Runnable() {
            @Override
            public void run() {
                centralizarMapa();
            }
        });

        atualizarInformacoesRotas();
    }
    private void adicionarMarcadores() {
        if (mMap == null) return;

        // Remover marcadores existentes
        if (origemMarker != null) origemMarker.remove();
        if (destinoMarker != null) destinoMarker.remove();


        origemMarker = mMap.addMarker(new MarkerOptions()
                .position(origem)
                .title("Origem")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        destinoMarker = mMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Destino")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        Log.d(TAG, "Marcadores adicionados ao mapa");
    }

    private void gerarRotasSimuladas() {
        rotasPontos.clear();

        // Rota 1 - Mais segura (maior número de pontos para simular caminho mais longo)
        List<LatLng> rota1 = new ArrayList<>();
        rota1.add(origem);

        rota1.add(new LatLng(origem.latitude + 0.003, origem.longitude + 0.002));
        rota1.add(new LatLng(origem.latitude + 0.005, origem.longitude + 0.005));
        rota1.add(new LatLng(destino.latitude - 0.004, destino.longitude - 0.001));
        rota1.add(new LatLng(destino.latitude - 0.002, destino.longitude - 0.0005));
        rota1.add(destino);
        rotasPontos.add(rota1);

        // Rota 2 - Segurança média
        List<LatLng> rota2 = new ArrayList<>();
        rota2.add(origem);
        rota2.add(new LatLng(origem.latitude + 0.002, origem.longitude + 0.004));
        rota2.add(new LatLng(destino.latitude - 0.001, destino.longitude - 0.003));
        rota2.add(destino);
        rotasPontos.add(rota2);

        // Rota 3 - Menos segura (caminho mais direto)
        List<LatLng> rota3 = new ArrayList<>();
        rota3.add(origem);
        rota3.add(new LatLng(origem.latitude + 0.001, origem.longitude + 0.001));
        rota3.add(destino);
        rotasPontos.add(rota3);

        Log.d(TAG, "Rotas simuladas geradas. Total de rotas: " + rotasPontos.size());
    }

    private void desenharRotas() {
        if (mMap == null) return;


        for (Polyline polyline : rotaPolylines) {
            polyline.remove();
        }
        rotaPolylines.clear();


        int[] cores = {COR_ROTA_MELHOR, COR_ROTA_MEDIA, COR_ROTA_PIOR};


        for (int i = 0; i < rotasPontos.size(); i++) {
            List<LatLng> pontos = rotasPontos.get(i);
            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(pontos)
                    .color(cores[i])
                    .width(10)
                    .geodesic(true));


            polyline.setZIndex(1.0f);

            rotaPolylines.add(polyline);
        }

        Log.d(TAG, "Rotas desenhadas no mapa");
    }

    private void selecionarRota(int indice) {
        if (indice < 0 || indice >= rotaPolylines.size()) return;


        selectedRouteIndex = indice;


        for (int i = 0; i < rotaPolylines.size(); i++) {
            Polyline polyline = rotaPolylines.get(i);
            int cor;
            float largura;
            float zIndex;

            if (i == indice) {

                cor = (i == 0) ? COR_ROTA_MELHOR : (i == 1) ? COR_ROTA_MEDIA : COR_ROTA_PIOR;
                largura = 14.0f;
                zIndex = 10.0f;
            } else {

                cor = Color.argb(
                        153,
                        Color.red((i == 0) ? COR_ROTA_MELHOR : (i == 1) ? COR_ROTA_MEDIA : COR_ROTA_PIOR),
                        Color.green((i == 0) ? COR_ROTA_MELHOR : (i == 1) ? COR_ROTA_MEDIA : COR_ROTA_PIOR),
                        Color.blue((i == 0) ? COR_ROTA_MELHOR : (i == 1) ? COR_ROTA_MEDIA : COR_ROTA_PIOR));
                largura = 8.0f;
                zIndex = 1.0f;
            }

            polyline.setColor(cor);
            polyline.setWidth(largura);
            polyline.setZIndex(zIndex);
        }

        Log.d(TAG, "Rota " + (indice + 1) + " selecionada");
    }

    private void destacarCardSelecionado(CardView cardSelecionado) {

        cardRota1.setCardBackgroundColor(Color.WHITE);
        cardRota2.setCardBackgroundColor(Color.WHITE);
        cardRota3.setCardBackgroundColor(Color.WHITE);


        cardRota1.setCardElevation(0f);
        cardRota2.setCardElevation(0f);
        cardRota3.setCardElevation(0f);


        cardSelecionado.setCardBackgroundColor(Color.parseColor("#F5F5F5"));  // Cinza muito claro
        cardSelecionado.setCardElevation(2f);


        if (selectedRouteIndex == 0) {
            indicatorRota1.setBackgroundResource(R.drawable.bg_rota_indicator_selected);
            indicatorRota2.setBackgroundResource(R.drawable.bg_rota_indicator);
            indicatorRota3.setBackgroundResource(R.drawable.bg_rota_indicator_danger);

            indicatorRota1.setText("39 min\nMelhor");
            indicatorRota2.setText("46 min");
            indicatorRota3.setText("55 min");
        } else if (selectedRouteIndex == 1) {
            indicatorRota1.setBackgroundResource(R.drawable.bg_rota_indicator_selected);
            indicatorRota2.setBackgroundResource(R.drawable.bg_rota_indicator);
            indicatorRota3.setBackgroundResource(R.drawable.bg_rota_indicator_danger);

            indicatorRota1.setText("39 min");
            indicatorRota2.setText("46 min\nSelecionada");
            indicatorRota3.setText("55 min");
        } else {
            indicatorRota1.setBackgroundResource(R.drawable.bg_rota_indicator_selected);
            indicatorRota2.setBackgroundResource(R.drawable.bg_rota_indicator);
            indicatorRota3.setBackgroundResource(R.drawable.bg_rota_indicator_danger);
            // Texto padrão para todas
            indicatorRota1.setText("39 min");
            indicatorRota2.setText("46 min");
            indicatorRota3.setText("55 min\nSelecionada");
        }
    }

    private void centralizarMapa() {
        if (mMap == null || origem == null || destino == null) return;

        // Construir os limites
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origem);
        builder.include(destino);

        for (List<LatLng> pontos : rotasPontos) {
            for (LatLng ponto : pontos) {
                builder.include(ponto);
            }
        }

        final LatLngBounds bounds = builder.build();
        final int padding = (int) (80 * getResources().getDisplayMetrics().density);

        // Adicionar um delay para garantir que o mapa tenha tamanho
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getWidth() > 0) {
            // Se o mapa já tem tamanho, podemos ajustar imediatamente
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } else {
            // Caso contrário, esperamos o layout ser calculado
            mapView.post(new Runnable() {
                @Override
                public void run() {
                    // Este código será executado após o layout ser calculado
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }
            });
        }

        Log.d(TAG, "Mapa centralizado para mostrar todas as rotas");
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void atualizarInformacoesRotas() {


        imgAvisoRota2.setColorFilter(Color.parseColor("#FFC107"));  // Cor âmbar
        imgAvisoRota3.setColorFilter(Color.parseColor("#FFC107"));  // Cor âmbar
        imgPolicia.setColorFilter(Color.parseColor("#03A9F4"));     // Cor azul claro
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (rotaPolylines != null) {
            for (Polyline polyline : rotaPolylines) {
                polyline.remove();
            }
            rotaPolylines.clear();
        }
    }
}