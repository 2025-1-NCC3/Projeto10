package br.com.fecapccp.ubergirls;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class activity_selecao_rota extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "RotaSeguraActivity";

    private GoogleMap mMap;
    private List<Polyline> rotaPolylines = new ArrayList<>();
    private List<List<LatLng>> rotasPontos = new ArrayList<>();

    // Cores das rotas
    private static final int COR_ROTA_MELHOR = Color.parseColor("#6200EE");  // Roxo (melhor rota)
    private static final int COR_ROTA_MEDIA = Color.parseColor("#9C27B0");   // Roxo médio
    private static final int COR_ROTA_PIOR = Color.parseColor("#E91E63");    // Rosa (pior)

    // TextView indicators
    private TextView indicatorRota1;
    private TextView indicatorRota2;
    private TextView indicatorRota3;

    // Cards para rotas
    private CardView cardRota1;
    private CardView cardRota2;
    private CardView cardRota3;

    private Button btnConfirmarPartida;
    private ImageView btnVoltar;
    private TextView textEnderecoOrigem;

    private TextView txtPesquisar;

    private Marker origemMarker;
    private Marker destinoMarker;

    // Coordenadas de origem e destino
    private LatLng origem;
    private LatLng destino;

    // Endereços textuais
    private String enderecoOrigem;
    private String enderecoDestino;

    private int selectedRouteIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_selecao_rota);

            Log.d(TAG, "Iniciando RotaSegura Activity");

            inicializarUI();

            // Obter os endereços passados da activity anterior
            obterEnderecos();

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
    private TextView txtTempoRota3, txtDistanciaRota3, txtViaRota3, txtInfoRota3,  txtAvisoRota3;


    private void inicializarUI() {
        indicatorRota1 = findViewById(R.id.indicatorRota1);
        indicatorRota2 = findViewById(R.id.indicatorRota2);
        indicatorRota3 = findViewById(R.id.indicatorRota3);

        cardRota1 = findViewById(R.id.cardRota1);
        cardRota2 = findViewById(R.id.cardRota2);
        cardRota3 = findViewById(R.id.cardRota3);

        btnConfirmarPartida = findViewById(R.id.btnConfirmarPartida);
        btnVoltar = findViewById(R.id.btnVoltar);
        textEnderecoOrigem = findViewById(R.id.textEnderecoOrigem);
        txtPesquisar =  findViewById(R.id.txtPesquisar);

        txtTempoRota1 = findViewById(R.id.txtTempoRota1);
        txtDistanciaRota1 = findViewById(R.id.txtDistanciaRota1);
        txtViaRota1 = findViewById(R.id.txtViaRota1);
        txtInfoRota1 = findViewById(R.id.txtInfoRota1);

        txtTempoRota2 = findViewById(R.id.txtTempoRota2);
        txtDistanciaRota2 = findViewById(R.id.txtDistanciaRota2);
        txtViaRota2 = findViewById(R.id.txtViaRota2);
        txtInfoRota2 = findViewById(R.id.txtInfoRota2);

        txtTempoRota3 = findViewById(R.id.txtTempoRota3);
        txtDistanciaRota3 = findViewById(R.id.txtDistanciaRota3);
        txtViaRota3 = findViewById(R.id.txtViaRota3);
        txtInfoRota3 = findViewById(R.id.txtInfoRota3);


    }

    private void obterEnderecos() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            enderecoOrigem = extras.getString("origem", "Endereço de origem");
            enderecoDestino = extras.getString("destino", "Endereço de destino");

            // Atualiza o texto na UI com o endereço de origem
            textEnderecoOrigem.setText(enderecoOrigem);

            // Geocodificar os endereços para obter as coordenadas
            geocodificarEnderecos();
        } else {
            // Caso não venha dados, usar padrões
            enderecoOrigem = "FECAP, São Paulo";
            enderecoDestino = "Avenida Paulista, São Paulo";
            textEnderecoOrigem.setText(enderecoOrigem);

            // Usar coordenadas padrão
            origem = new LatLng(-23.5505, -46.6333);  // Centro de São Paulo
            destino = new LatLng(-23.5905, -46.6933); // Exemplo de destino
        }
    }

    private double calcularDistanciaRota(int indiceRota) {
        // Calcular distância em linha reta em km
        float[] resultado = new float[1];
        android.location.Location.distanceBetween(
                origem.latitude, origem.longitude,
                destino.latitude, destino.longitude,
                resultado);

        float distanciaBase = resultado[0] / 1000; // em km

        // Ajustar distância com base na rota selecionada
        switch(indiceRota) {
            case 0: return distanciaBase;                // Rota padrão
            case 1: return distanciaBase * 1.1;          // Rota um pouco mais longa
            case 2: return distanciaBase * 0.9;          // Rota mais curta
            default: return distanciaBase;
        }
    }

    private int calcularTempoRota(int indiceRota) {
        // Calcular distância em linha reta em km
        double distanciaKm = calcularDistanciaRota(indiceRota);

        // Calcular tempo base (velocidade média: 30km/h)
        int tempoBaseMinutos = Math.round((float)(distanciaKm / 30) * 60);

        // Ajustar tempo com base na rota
        switch(indiceRota) {
            case 0: return Math.max(tempoBaseMinutos, 10);                // Melhor rota
            case 1: return Math.max(Math.round(tempoBaseMinutos * 1.2f), 12);  // Rota média
            case 2: return Math.max(Math.round(tempoBaseMinutos * 1.4f), 15);  // Rota menos segura
            default: return tempoBaseMinutos;
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
                // Caso não consiga geocodificar, usar coordenada padrão
                Log.w(TAG, "Não foi possível geocodificar o endereço de origem");
                origem = new LatLng(-23.5505, -46.6333);
            }

            // Geocodificar o endereço de destino
            List<Address> listaDestino = geocoder.getFromLocationName(enderecoDestino, 1);
            if (listaDestino != null && listaDestino.size() > 0) {
                Address endereco = listaDestino.get(0);
                destino = new LatLng(endereco.getLatitude(), endereco.getLongitude());
                Log.d(TAG, "Destino geocodificado: " + destino.toString());
            } else {
                // Caso não consiga geocodificar, usar coordenada padrão
                Log.w(TAG, "Não foi possível geocodificar o endereço de destino");
                destino = new LatLng(-23.5905, -46.6933);
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao geocodificar endereços", e);
            Toast.makeText(this, "Erro ao obter coordenadas dos endereços", Toast.LENGTH_SHORT).show();

            // Em caso de erro, usar coordenadas padrão
            origem = new LatLng(-23.5505, -46.6333);
            destino = new LatLng(-23.5905, -46.6933);
        }
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

        // Modificação aqui: após confirmar a rota, navega para EscolhaRota
        btnConfirmarPartida.setOnClickListener(v -> {
            // Calcular distância e tempo da rota selecionada
            double distanciaEstimada = calcularDistanciaRota(selectedRouteIndex);
            int tempoEstimado = calcularTempoRota(selectedRouteIndex);





            // Criar intent para navegar para EscolhaRota
            Intent intent = new Intent(activity_selecao_rota.this, EscolhaRota.class);

            // Passar todos os dados necessários
            intent.putExtra("origem", enderecoOrigem);
            intent.putExtra("destino", enderecoDestino);
            intent.putExtra("rotaSelecionada", selectedRouteIndex);
            intent.putExtra("distancia", distanciaEstimada);
            intent.putExtra("tempo", tempoEstimado);

            // Iniciar a activity
            startActivity(intent);
        });

        btnVoltar.setOnClickListener(v -> {
            finish(); // Volta para a activity anterior
        });

        txtPesquisar.setOnClickListener(v -> {
            Intent voltarIntent = new Intent(activity_selecao_rota.this, PesquisaActivity.class);
            startActivity(voltarIntent);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Mapa carregado com sucesso");

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Verificar se as coordenadas são válidas
        if (origem == null || destino == null) {
            Log.e(TAG, "Coordenadas de origem ou destino são nulas");
            Toast.makeText(this, "Erro ao carregar coordenadas", Toast.LENGTH_SHORT).show();
            return;
        }

        adicionarMarcadores();
        gerarRotasSimuladas();
        desenharRotas();
        selecionarRota(0);
        destacarCardSelecionado(cardRota1);

        // Ajustamos a visualização do mapa após um pequeno delay para garantir que ele esteja renderizado
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView != null) {
            mapView.post(() -> centralizarMapa());
        }

        // Calcular e atualizar tempos/distâncias com base nas rotas geradas
        calcularTemposDistancias();
    }

    private void adicionarMarcadores() {
        if (mMap == null) return;

        // Remover marcadores existentes
        if (origemMarker != null) origemMarker.remove();
        if (destinoMarker != null) destinoMarker.remove();

        origemMarker = mMap.addMarker(new MarkerOptions()
                .position(origem)
                .title("Origem: " + enderecoOrigem)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        destinoMarker = mMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Destino: " + enderecoDestino)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        Log.d(TAG, "Marcadores adicionados ao mapa");
    }

    private void gerarRotasSimuladas() {
        rotasPontos.clear();

        // Calcular distância aproximada entre os pontos (em graus)
        double distancia = Math.sqrt(
                Math.pow(destino.latitude - origem.latitude, 2) +
                        Math.pow(destino.longitude - origem.longitude, 2)
        );

        // Ajustar o fator de variação com base na distância
        double fatorBase = distancia / 30; // Proporcional à distância

        // Rota 1 - Mais segura (melhor)
        List<LatLng> rota1 = criarRotaSimulada(origem, destino, fatorBase);
        rotasPontos.add(rota1);

        // Rota 2 - Segurança média
        List<LatLng> rota2 = criarRotaSimulada(origem, destino, fatorBase * 2);
        rotasPontos.add(rota2);

        // Rota 3 - Menos segura
        List<LatLng> rota3 = criarRotaSimulada(origem, destino, fatorBase * 3);
        rotasPontos.add(rota3);

        Log.d(TAG, "Rotas simuladas geradas. Total de rotas: " + rotasPontos.size());
    }

    private List<LatLng> criarRotaSimulada(LatLng origem, LatLng destino, double fatorVariacao) {
        // Em um app real, você usaria as direções da API do Google Maps
        List<LatLng> pontos = new ArrayList<>();
        pontos.add(origem);

        // Adicionar pontos intermediários para simular uma rota
        double lat = origem.latitude;
        double lng = origem.longitude;
        double latDest = destino.latitude;
        double lngDest = destino.longitude;

        // Determinar o número de pontos com base na distância
        double distancia = Math.sqrt(
                Math.pow(latDest - lat, 2) +
                        Math.pow(lngDest - lng, 2)
        );

        int numPontos = Math.max(5, (int)(distancia * 500)); // Min 5 pontos
        numPontos = Math.min(numPontos, 20); // Max 20 pontos

        double latStep = (latDest - lat) / numPontos;
        double lngStep = (lngDest - lng) / numPontos;

        for (int i = 1; i < numPontos; i++) {
            // Adicionar uma variação para diferenciar as rotas
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

    private void desenharRotas() {
        if (mMap == null) return;

        // Limpar polylines anteriores
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

    private void calcularTemposDistancias() {
        // Calcular distância em linha reta em metros
        float[] resultado = new float[1];
        android.location.Location.distanceBetween(
                origem.latitude, origem.longitude,
                destino.latitude, destino.longitude,
                resultado);

        float distanciaMetros = resultado[0];
        float distanciaKm = distanciaMetros / 1000;

        // Calcular tempos aproximados (velocidade média: 30km/h)
        int tempoMelhorMinutos = Math.round((distanciaKm / 30) * 60);
        int tempoMedioMinutos = Math.round(tempoMelhorMinutos * 1.2f);
        int tempoPioresMinutos = Math.round(tempoMelhorMinutos * 1.4f);

        // Ajustar para valores mínimos razoáveis
        tempoMelhorMinutos = Math.max(tempoMelhorMinutos, 10);
        tempoMedioMinutos = Math.max(tempoMedioMinutos, 12);
        tempoPioresMinutos = Math.max(tempoPioresMinutos, 15);

        // Atualizar textos das rotas
        indicatorRota1.setText(tempoMelhorMinutos + " min\nMelhor");
        indicatorRota2.setText(tempoMedioMinutos + " min");
        indicatorRota3.setText(tempoPioresMinutos + " min");

        // Atualizar as informações nos cards
        txtTempoRota1.setText(tempoMelhorMinutos + " min");
        txtDistanciaRota1.setText(String.format("%.1f km", distanciaKm));
        txtViaRota1.setText("Via melhor rota");
        txtInfoRota1.setText("Melhor rota, trânsito normal");

        txtTempoRota2.setText(tempoMedioMinutos + " min");
        txtDistanciaRota2.setText(String.format("%.1f km", distanciaKm * 1.1f));
        txtViaRota2.setText("Via alternativa");
        txtInfoRota2.setText("Rota alternativa, trânsito moderado");

        txtTempoRota3.setText(tempoPioresMinutos + " min");
        txtDistanciaRota3.setText(String.format("%.1f km", distanciaKm * 0.9f));
        txtViaRota3.setText("Via alternativa");
        txtInfoRota3.setText("Rota com áreas de atenção");
    }

    private void destacarCardSelecionado(CardView cardSelecionado) {
        // Resetar cores de todos os cards
        cardRota1.setCardBackgroundColor(Color.WHITE);
        cardRota2.setCardBackgroundColor(Color.WHITE);
        cardRota3.setCardBackgroundColor(Color.WHITE);

        // Resetar elevação
        cardRota1.setCardElevation(0f);
        cardRota2.setCardElevation(0f);
        cardRota3.setCardElevation(0f);

        // Destacar o card selecionado
        cardSelecionado.setCardBackgroundColor(Color.parseColor("#F5F5F5"));  // Cinza muito claro
        cardSelecionado.setCardElevation(2f);

        // Atualizar indicadores visuais
        if (selectedRouteIndex == 0) {
            indicatorRota1.setBackgroundResource(R.drawable.bg_rota_indicator_selected);
            indicatorRota2.setBackgroundResource(R.drawable.bg_rota_indicator);
            indicatorRota3.setBackgroundResource(R.drawable.bg_rota_indicator_danger);

            indicatorRota1.setText(indicatorRota1.getText().toString().split("\n")[0] + "\nMelhor");
            indicatorRota2.setText(indicatorRota2.getText().toString().split("\n")[0]);
            indicatorRota3.setText(indicatorRota3.getText().toString().split("\n")[0]);
        } else if (selectedRouteIndex == 1) {
            indicatorRota1.setBackgroundResource(R.drawable.bg_rota_indicator_selected);
            indicatorRota2.setBackgroundResource(R.drawable.bg_rota_indicator);
            indicatorRota3.setBackgroundResource(R.drawable.bg_rota_indicator_danger);

            indicatorRota1.setText(indicatorRota1.getText().toString().split("\n")[0]);
            indicatorRota2.setText(indicatorRota2.getText().toString().split("\n")[0] + "\nSelecionada");
            indicatorRota3.setText(indicatorRota3.getText().toString().split("\n")[0]);
        } else {
            indicatorRota1.setBackgroundResource(R.drawable.bg_rota_indicator_selected);
            indicatorRota2.setBackgroundResource(R.drawable.bg_rota_indicator);
            indicatorRota3.setBackgroundResource(R.drawable.bg_rota_indicator_danger);

            indicatorRota1.setText(indicatorRota1.getText().toString().split("\n")[0]);
            indicatorRota2.setText(indicatorRota2.getText().toString().split("\n")[0]);
            indicatorRota3.setText(indicatorRota3.getText().toString().split("\n")[0] + "\nSelecionada");
        }
    }

    private void centralizarMapa() {
        if (mMap == null || origem == null || destino == null) return;

        // Construir os limites
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origem);
        builder.include(destino);

        // Incluir todos os pontos de todas as rotas
        for (List<LatLng> pontos : rotasPontos) {
            for (LatLng ponto : pontos) {
                builder.include(ponto);
            }
        }

        final LatLngBounds bounds = builder.build();
        final int padding = (int) (80 * getResources().getDisplayMetrics().density);

        // Ajustar a visualização do mapa
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            Log.e(TAG, "Erro ao ajustar câmera", e);
            // Fallback para um zoom básico em caso de erro
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origem, 12));
        }

        Log.d(TAG, "Mapa centralizado para mostrar todas as rotas");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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