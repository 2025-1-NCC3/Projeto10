package br.com.fecapccp.ubergirls;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

public class EscolhaRota extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Polyline> rotaPolylines = new ArrayList<>();

    // Cores das rotas
    private static final int COR_ROTA_SEGURA = Color.parseColor("#6200EE");  // Roxo escuro
    private static final int COR_ROTA_MEDIA = Color.parseColor("#9C27B0");    // Roxo médio
    private static final int COR_ROTA_PERIGOSA = Color.parseColor("#E91E63"); // Rosa/vermelho

    // TextViews para as informações de rotas
    private TextView textRotaSegura;
    private TextView textRotaMedia;
    private TextView textRotaPeriogosa;
    private Button btnConfirmarPartida;
    private CardView cardMelhorRota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escolharota_main);

        // Inicializar elementos da UI
        textRotaSegura = findViewById(R.id.textRotaSegura);
        textRotaMedia = findViewById(R.id.textRotaMedia);
        textRotaPeriogosa = findViewById(R.id.textRotaPeriogosa);
        btnConfirmarPartida = findViewById(R.id.btnConfirmarPartida);
        cardMelhorRota = findViewById(R.id.cardMelhorRota);

        // Obter o SupportMapFragment e ser notificado quando o mapa estiver pronto para uso
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configurar listeners para os elementos da UI
        configurarListeners();
    }

    private void configurarListeners() {
        // Listener para o botão de confirmar partida
        btnConfirmarPartida.setOnClickListener(v -> {
            Toast.makeText(EscolhaRota.this, "Partida confirmada!", Toast.LENGTH_SHORT).show();
            // Aqui você poderia iniciar a próxima Activity
        });

        // Listeners para as opções de rota
        textRotaSegura.setOnClickListener(v -> selecionarRota(0));
        textRotaMedia.setOnClickListener(v -> selecionarRota(1));
        textRotaPeriogosa.setOnClickListener(v -> selecionarRota(2));

        // Listener para o card da melhor rota
        cardMelhorRota.setOnClickListener(v -> {
            // Poderia exibir mais detalhes sobre a rota selecionada
            Toast.makeText(EscolhaRota.this, "Detalhes da melhor rota", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Customizar o estilo do mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);

        // Exemplo de coordenadas para São Paulo (baseado na imagem)
        // Estas coordenadas são aproximadas e devem ser substituídas pelas reais
        LatLng origem = new LatLng(-23.5505, -46.6333);  // Centro de São Paulo
        LatLng destino = new LatLng(-23.5905, -46.6933); // Exemplo de destino

        // Adicionar marcadores para origem e destino
        mMap.addMarker(new MarkerOptions()
                .position(origem)
                .title("Origem")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Destino")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Desenhar as três rotas alternativas
        desenharRotas(origem, destino);

        // Ajustar câmera para mostrar toda a rota
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origem);
        builder.include(destino);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    private void desenharRotas(LatLng origem, LatLng destino) {
        // Limpar polylines anteriores
        for (Polyline polyline : rotaPolylines) {
            polyline.remove();
        }
        rotaPolylines.clear();

        // Rota segura (melhor) - Rota roxa escura
        List<LatLng> pontosRotaSegura = criarRotaSimulada(origem, destino, 0.005);
        Polyline rotaSegura = mMap.addPolyline(new PolylineOptions()
                .addAll(pontosRotaSegura)
                .color(COR_ROTA_SEGURA)
                .width(10));
        rotaPolylines.add(rotaSegura);

        // Rota média - Rota roxa média
        List<LatLng> pontosRotaMedia = criarRotaSimulada(origem, destino, 0.01);
        Polyline rotaMedia = mMap.addPolyline(new PolylineOptions()
                .addAll(pontosRotaMedia)
                .color(COR_ROTA_MEDIA)
                .width(10));
        rotaPolylines.add(rotaMedia);

        // Rota perigosa - Rota rosa/vermelha
        List<LatLng> pontosRotaPeriogsa = criarRotaSimulada(origem, destino, 0.015);
        Polyline rotaPeriogsa = mMap.addPolyline(new PolylineOptions()
                .addAll(pontosRotaPeriogsa)
                .color(COR_ROTA_PERIGOSA)
                .width(10));
        rotaPolylines.add(rotaPeriogsa);

        // Selecionar a rota segura por padrão
        selecionarRota(0);
    }

    private List<LatLng> criarRotaSimulada(LatLng origem, LatLng destino, double fatorVariacao) {
        // Este método cria uma rota simulada entre origem e destino
        // Em um app real, você usaria as direções da API do Google Maps
        List<LatLng> pontos = new ArrayList<>();
        pontos.add(origem);

        // Adicionar pontos intermediários para simular uma rota
        double lat = origem.latitude;
        double lng = origem.longitude;
        double latDest = destino.latitude;
        double lngDest = destino.longitude;

        double latStep = (latDest - lat) / 5;
        double lngStep = (lngDest - lng) / 5;

        for (int i = 1; i < 5; i++) {
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

    private void selecionarRota(int indiceRota) {
        // Atualizar a visualização das rotas no mapa
        for (int i = 0; i < rotaPolylines.size(); i++) {
            Polyline polyline = rotaPolylines.get(i);
            if (i == indiceRota) {
                polyline.setWidth(15); // Rota selecionada com linha mais grossa
                polyline.setZIndex(1); // Exibir acima das outras
            } else {
                polyline.setWidth(7);  // Rotas não selecionadas com linha mais fina
                polyline.setZIndex(0);
            }
        }

        // Atualizar a interface com as informações da rota selecionada
        switch (indiceRota) {
            case 0: // Rota segura
                atualizarInfoRota("39 min", "12,2 km", "Por Av. Pres. Wilson São Paulo", "Melhor rota, trânsito normal");
                break;
            case 1: // Rota média
                atualizarInfoRota("46 min", "14,5 km", "Por Av. Marginal Tietê", "Rota alternativa, trânsito moderado");
                break;
            case 2: // Rota perigosa
                atualizarInfoRota("55 min", "16,8 km", "Por Av. Inajar de Souza", "Rota com áreas de atenção, evitar à noite");
                break;
        }
    }

    private void atualizarInfoRota(String tempo, String distancia, String via, String info) {
        // Atualizar as informações no card da rota
        TextView textTempoMelhor = findViewById(R.id.textTempoMelhor);
        TextView textKmMelhor = findViewById(R.id.textKmMelhor);
        TextView textRotaMelhor = findViewById(R.id.textRotaMelhor);
        TextView textInfoMelhor = findViewById(R.id.textInfoMelhor);

        textTempoMelhor.setText(tempo);
        textKmMelhor.setText(distancia);
        textRotaMelhor.setText(via);
        textInfoMelhor.setText(info);

        // Alterar a cor do texto de informação baseado na rota
        if (info.contains("Melhor rota")) {
            textInfoMelhor.setTextColor(COR_ROTA_SEGURA);
        } else if (info.contains("alternativa")) {
            textInfoMelhor.setTextColor(COR_ROTA_MEDIA);
        } else {
            textInfoMelhor.setTextColor(COR_ROTA_PERIGOSA);
        }
    }
}