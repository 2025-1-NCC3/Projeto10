package br.com.fecapccp.ubergirls;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class CorridaSimulacaoActivity extends AppCompatActivity {

    private ImageView imagemCarro;
    private TextView textoStatus;
    private ProgressBar progressoViagem;
    private TextView textoMotorista;
    private TextView textoCarro;
    private TextView textoTempo;


    private final Handler handler = new Handler(Looper.getMainLooper());


    private int etapaAtual = 0;
    private final int TOTAL_ETAPAS = 4;


    private static final int DURACAO_ANIMACAO_CARRO = 1500;
    private static final int DURACAO_TOTAL_VIAGEM = 60000; // 60 segundos para simulação completa


    private final String[] statusMensagens = {
            "Buscando Motorista",
            "Motorista a caminho",
            "Você está a caminho",
            "Chegando ao destino",
            "Viagem finalizada"
    };


    private String enderecoOrigem;
    private String enderecoDestino;
    private int tempoEstimado;
    private double distanciaEstimada;
    private int rotaSelecionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida_simulacao);

        // Inicializar elementos da UI
        inicializarElementosUI();

        // Obter dados da intent (da tela EscolhaRota)
        obterDadosDaViagem();

        // Iniciar simulação da corrida
        iniciarSimulacao();
    }

    private void inicializarElementosUI() {
        imagemCarro = findViewById(R.id.imagemCarro);
        textoStatus = findViewById(R.id.textoStatus);
        progressoViagem = findViewById(R.id.progressoViagem);

        textoMotorista = findViewById(R.id.textoMotorista);
        textoCarro = findViewById(R.id.textoCarro);
        textoTempo = findViewById(R.id.textoTempo);

        progressoViagem.setIndeterminate(true);

        // Carregar GIF usando Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.carro_animado)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imagemCarro);
    }


    private void obterDadosDaViagem() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            enderecoOrigem = extras.getString("origem", "Local de origem");
            enderecoDestino = extras.getString("destino", "Local de destino");
            tempoEstimado = extras.getInt("tempo", 12); // Tempo padrão: 12 minutos
            distanciaEstimada = extras.getDouble("distancia", 5.0); // Distância padrão: 5 km
            rotaSelecionada = extras.getInt("rotaSelecionada", 0);
        } else {

            enderecoOrigem = "Origem não especificada";
            enderecoDestino = "Destino não especificado";
            tempoEstimado = 12;
            distanciaEstimada = 5.0;
            rotaSelecionada = 0;
        }
    }

    private void iniciarSimulacao() {


        atualizarStatusViagem(0);


        handler.postDelayed(() -> atualizarStatusViagem(1), 5000);


        handler.postDelayed(() -> {
            atualizarStatusViagem(2);


            progressoViagem.setIndeterminate(false);
            progressoViagem.setMax(100);
            progressoViagem.setProgress(0);


            iniciarAnimacaoProgresso();

        }, 15000);


        handler.postDelayed(() -> atualizarStatusViagem(3), 45000);


        handler.postDelayed(() -> atualizarStatusViagem(4), DURACAO_TOTAL_VIAGEM);
    }


    private void iniciarAnimacaoProgresso() {

        long tempoRestante = DURACAO_TOTAL_VIAGEM - 15000; // 15 segundos já se passaram

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                progressoViagem, "progress", 0, 100);
        progressAnimator.setDuration(tempoRestante);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            atualizarTempoRestante(progress);
        });
        progressAnimator.start();
    }

    private void atualizarTempoRestante(int progresso) {

        int tempoRestoMin = (int) ((100 - progresso) * tempoEstimado / 100);
        int tempoRestoSeg = (int) (((100 - progresso) * tempoEstimado * 60 / 100) % 60);


        String textoTempoRestante = String.format("Tempo estimado: %d min %02d s",
                tempoRestoMin, tempoRestoSeg);
        if (textoTempo != null) {
            textoTempo.setText(textoTempoRestante);
        }
    }

    private void atualizarStatusViagem(int etapa) {
        etapaAtual = etapa;


        textoStatus.setText(statusMensagens[etapa]);

        // Atualizar cor do texto de status conforme a etapa
        switch (etapa) {
            case 0: // Buscando motorista
            case 1: // Motorista a caminho
                textoStatus.setTextColor(getResources().getColor(android.R.color.holo_purple));
                break;
            case 2: // Em viagem
            case 3: // Chegando
                textoStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case 4: // Finalizado
                textoStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                finalizarViagem();
                break;
        }


        atualizarInformacoesAdicionais(etapa);
    }

    private void atualizarInformacoesAdicionais(int etapa) {
        // Array de nomes de motoristas possíveis
        String[] nomes = {"Ana Silva", "Julia Santos", "Carla Oliveira", "Beatriz Lima", "Daniela Costa"};

        // Array de modelos de carros possíveis
        String[] modelos = {"Fiat Mobi - Rosa", "VW Up - Lilás", "Renault Kwid - Rosa", "Toyota Etios - Rosa"};

        // Array de placas possíveis
        String[] placas = {"ABC1234", "UBG5678", "GRL9012", "FEM3456", "SEG7890"};


        if (etapa == 0) {
            int indiceNome = new Random().nextInt(nomes.length);
            int indiceModelo = new Random().nextInt(modelos.length);
            int indicePlaca = new Random().nextInt(placas.length);

            if (textoMotorista != null) {
                textoMotorista.setText("Motorista: " + nomes[indiceNome]);
            }

            if (textoCarro != null) {
                textoCarro.setText("Carro: " + modelos[indiceModelo] + " - " + placas[indicePlaca]);
            }

            if (textoTempo != null) {
                textoTempo.setText("Tempo estimado: " + tempoEstimado + " min");
            }
        }


        if (etapa == 1) {
            if (textoTempo != null) {
                textoTempo.setText("Chegada em: 3 min");
            }
        }
    }

    private void finalizarViagem() {

        if (imagemCarro != null) {
            imagemCarro.clearAnimation();
        }


        if (progressoViagem != null) {
            progressoViagem.setProgress(100);
            progressoViagem.setIndeterminate(false);
        }


        if (textoStatus != null) {
            textoStatus.setText("Viagem finalizada");
        }

        if (textoTempo != null) {
            textoTempo.setText("Tempo de viagem: " + tempoEstimado + " min");
        }


        handler.postDelayed(() -> {

            Intent intent = new Intent(CorridaSimulacaoActivity.this, TelaHomeActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, 3000);
    }
}