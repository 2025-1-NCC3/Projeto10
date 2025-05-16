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

    // referências dos TextView de horário
    private TextView textTempoPrincipal;
    private TextView textTempoSec;
    private TextView textTempoTer;

    private String opcaoSelecionada = "UberGirls";

    // Armazenar os endereços de origem e destino
    private String enderecoOrigem;
    private String enderecoDestino;
    private LatLng pontoOrigem;
    private LatLng pontoDestino;
    private Marker marcadorOrigem;
    private Marker marcadorDestino;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Chave da API do Google (use a mesma que está na PesquisaActivity)
    private static final String API_KEY = "AIzaSyD6OQbHAg8n3-qHusRR-KCv61iICiYhjVI";

    // Zoom inicial para o ponto de origem
    private static final float ZOOM_ORIGEM = 16f; // Aumentado para proporcionar um zoom mais próximo

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

        // Obtém os dados de origem e destino da intent
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

        // encontra os TextView de horário
        textTempoPrincipal = findViewById(R.id.textTempoPrincipal);
        textTempoSec = findViewById(R.id.textTempoSec);
        textTempoTer = findViewById(R.id.textTempoTer);

        btnEscolha = findViewById(R.id.btnEscolha);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Configurar listeners
        imgUberX.setOnClickListener(v -> trocarComPrincipal(imgUberX, textUberX));
        textUberX.setOnClickListener(v -> trocarComPrincipal(imgUberX, textUberX));

        imgComfort.setOnClickListener(v -> trocarComPrincipal(imgComfort, textComfort));
        textComfort.setOnClickListener(v -> trocarComPrincipal(imgComfort, textComfort));

        atualizarBotao();

        btnEscolha.setOnClickListener(v -> {

            Intent escolhaIntent = new Intent(UberGirlsActivity.this, activity_selecao_rota.class);

            // Passar endereços de origem e destino
            escolhaIntent.putExtra("origem", enderecoOrigem);
            escolhaIntent.putExtra("destino", enderecoDestino);

            // Inicia a activity de seleção de rota
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

        // Configurações do mapa
        mMap.setTrafficEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Definir estilo de mapa otimizado para navegação
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Converter os endereços em coordenadas e traçar a rota
            geocodeEnderecos();
        }
    }

    private void geocodeEnderecos() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            // Geocodificação do endereço de origem com retrocompatibilidade para diferentes versões do Android
            geocodificarEndereco(geocoder, enderecoOrigem, true);
        } catch (IOException e) {
            Log.e("MAP_DEBUG", "Erro ao geocodificar endereços: " + e.getMessage());
            Toast.makeText(this, "Erro ao processar endereços", Toast.LENGTH_SHORT).show();
        }
    }

    private void geocodificarEndereco(Geocoder geocoder, String endereco, boolean isOrigem) throws IOException {
        // Método que lida com a geocodificação de forma mais robusta
        List<Address> enderecos = geocoder.getFromLocationName(endereco, 1);

        if (enderecos != null && !enderecos.isEmpty()) {
            Address addr = enderecos.get(0);
            LatLng ponto = new LatLng(addr.getLatitude(), addr.getLongitude());

            if (isOrigem) {
                pontoOrigem = ponto;
                Log.d("GEOCODE_DEBUG", "Origem geocodificada: " + pontoOrigem.latitude + "," + pontoOrigem.longitude);

                // Focar imediatamente no ponto de origem com zoom adequado
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));

                // Continuar com o destino
                try {
                    geocodificarEndereco(geocoder, enderecoDestino, false);
                } catch (IOException e) {
                    Log.e("MAP_DEBUG", "Erro ao geocodificar destino: " + e.getMessage());
                    Toast.makeText(this, "Erro ao processar endereço de destino", Toast.LENGTH_SHORT).show();
                }
            } else {
                pontoDestino = ponto;
                Log.d("GEOCODE_DEBUG", "Destino geocodificado: " + pontoDestino.latitude + "," + pontoDestino.longitude);

                // Agora temos as coordenadas de origem e destino, traçar a rota
                tracarRota();

                // Ativamos a localização após geocodificar para não interferir com o foco no ponto de origem
                ativarLocalizacao();
            }
        } else {
            String tipo = isOrigem ? "origem" : "destino";
            Toast.makeText(this, "Não foi possível encontrar o endereço de " + tipo, Toast.LENGTH_SHORT).show();

            if (isOrigem) {
                // Se não conseguimos o ponto de origem, tentamos usar a localização atual
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

                // Focar na localização atual
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));

                // Continuar com a geocodificação do destino
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

        // Remover marcadores anteriores se existirem
        if (marcadorOrigem != null) marcadorOrigem.remove();
        if (marcadorDestino != null) marcadorDestino.remove();

        // Adicionar marcadores para a origem e destino com estilos diferentes
        marcadorOrigem = mMap.addMarker(new MarkerOptions()
                .position(pontoOrigem)
                .title("Origem: " + enderecoOrigem)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        marcadorDestino = mMap.addMarker(new MarkerOptions()
                .position(pontoDestino)
                .title("Destino: " + enderecoDestino)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // IMPORTANTE: Garantir que o mapa esteja focado na origem primeiro
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));

        // Criar contexto para a API Directions com configurações otimizadas
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

            // Log para depuração
            Log.d("DIRECTIONS_DEBUG", "Solicitando rota de " + origem + " para " + destino);

            // Solicitar rota otimizada para veículos
            DirectionsResult resultado = DirectionsApi.newRequest(context)
                    .mode(TravelMode.DRIVING)  // Modo veicular
                    .origin(origem)
                    .destination(destino)
                    .optimizeWaypoints(true)   // Otimizar waypoints
                    .await();

            // Processar o resultado
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

                // Calcular distância e tempo estimado
                long duracaoSegundos = 0;
                float distanciaMetros = 0;

                if (rota.legs != null && rota.legs.length > 0) {
                    duracaoSegundos = rota.legs[0].duration.inSeconds;
                    distanciaMetros = rota.legs[0].distance.inMeters;

                    Log.d("DIRECTIONS_DEBUG", "Duração: " + duracaoSegundos + "s, Distância: " + distanciaMetros + "m");
                }

                // Atualizar informações de tempo e distância
                atualizarInfoRota(duracaoSegundos, distanciaMetros);

                // Desenhar a rota no mapa
                List<LatLng> pontos = PolyUtil.decode(rota.overviewPolyline.getEncodedPath());

                // Remover qualquer linha de rota existente e manter os marcadores
                mMap.clear();

                // Re-adicionar os marcadores após limpar o mapa
                marcadorOrigem = mMap.addMarker(new MarkerOptions()
                        .position(pontoOrigem)
                        .title("Origem: " + enderecoOrigem)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                marcadorDestino = mMap.addMarker(new MarkerOptions()
                        .position(pontoDestino)
                        .title("Destino: " + enderecoDestino)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                // Adicionar a linha da rota com estilo otimizado para visualização
                PolylineOptions linhaDaRota = new PolylineOptions()
                        .addAll(pontos)
                        .width(12)  // Linha um pouco mais grossa
                        .color(Color.parseColor("#4285F4"))  // Azul do Google Maps
                        .geodesic(true);  // Seguir a curvatura da Terra

                mMap.addPolyline(linhaDaRota);

                // FOCO INICIAL: Garantir que começamos focados no ponto de origem com zoom apropriado
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));

                // Após um delay, oferecemos opção de ver a rota completa
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Ajustar a câmera para exibir toda a rota com padding
                    if (pontos.size() > 1) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        // Incluir todos os pontos da rota para um melhor enquadramento
                        for (LatLng ponto : pontos) {
                            builder.include(ponto);
                        }

                        LatLngBounds limites = builder.build();
                        int padding = 200; // padding em pixels

                        // Animação suave para mostrar toda a rota
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(limites, padding));
                    }
                }, 3000); // 3 segundos de delay para dar tempo de visualizar o ponto de partida
            } else {
                Log.e("DIRECTIONS_DEBUG", "Nenhuma rota encontrada no resultado");
                mostrarRotaAlternativa();
            }
        });
    }

    private void mostrarRotaAlternativa() {
        // Método alternativo simples caso a API falhe
        mMap.clear(); // Limpa o mapa

        // Re-adiciona os marcadores
        marcadorOrigem = mMap.addMarker(new MarkerOptions()
                .position(pontoOrigem)
                .title("Origem: " + enderecoOrigem)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        marcadorDestino = mMap.addMarker(new MarkerOptions()
                .position(pontoDestino)
                .title("Destino: " + enderecoDestino)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Adiciona uma linha direta entre os pontos
        PolylineOptions linhaDireta = new PolylineOptions()
                .add(pontoOrigem)
                .add(pontoDestino)
                .width(10)
                .color(Color.RED)
                .geodesic(true);  // Para seguir a curvatura da Terra

        mMap.addPolyline(linhaDireta);

        // MODIFICAÇÃO: Primeiro foca no ponto de origem com zoom adequado
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pontoOrigem, ZOOM_ORIGEM));

        // Estimar tempo e distância em linha reta
        estimarInfoRotaSimples();

        // Após um delay maior para garantir que o usuário veja bem o ponto de partida
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Ajustar a câmera para exibir toda a rota com padding maior
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pontoOrigem);
            builder.include(pontoDestino);
            LatLngBounds limites = builder.build();

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(limites, 200));
        }, 3000);  // 3 segundos
    }

    private void estimarInfoRotaSimples() {
        // Calcula a distância em linha reta
        float[] results = new float[1];
        Location.distanceBetween(
                pontoOrigem.latitude, pontoOrigem.longitude,
                pontoDestino.latitude, pontoDestino.longitude,
                results);

        float distanciaMetros = results[0];

        // Estima tempo considerando velocidade média de 30 km/h (mais realista em ambiente urbano)
        // e adiciona um fator de 1.3x para compensar pela não-linearidade das vias
        float distanciaAjustada = distanciaMetros * 1.3f;
        long duracaoSegundos = (long) (distanciaAjustada / (30 * 1000 / 3600));

        // Atualiza as informações na interface
        atualizarInfoRota(duracaoSegundos, distanciaAjustada);
    }

    private void atualizarInfoRota(long duracaoSegundos, float distanciaMetros) {
        // Converter duração para minutos
        int minutos = (int) (duracaoSegundos / 60);

        // Converter distância para quilômetros
        float kmDistancia = distanciaMetros / 1000;

        // Formatar para exibição
        String duracao = minutos + " min";
        String distancia = String.format(Locale.getDefault(), "%.1f km", kmDistancia);

        // Atualizar os TextViews de tempo
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

            // Não vamos focar na localização atual do usuário, já que queremos
            // priorizar o ponto de origem da rota
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();

            // Se não temos pontos de origem ainda, podemos tentar usar a localização atual
            if (pontoOrigem == null) {
                obterLocalizacaoAtualComoOrigem();
            }
        } else {
            Log.e("MAP_DEBUG", "Permissão de localização negada.");

            // Mesmo sem permissão, tentamos geocodificar os endereços
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

    // formata e atualiza o horário nos três TextView
    private void atualizarHorarios() {
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());
        String sufixo = " - tempo estimado";

        // Só atualiza se não tivermos informações da rota real
        if (pontoOrigem == null || pontoDestino == null) {
            textTempoPrincipal.setText(hora + sufixo);
            textTempoSec.setText(hora + sufixo);
            textTempoTer.setText(hora + sufixo);
        }
    }
}