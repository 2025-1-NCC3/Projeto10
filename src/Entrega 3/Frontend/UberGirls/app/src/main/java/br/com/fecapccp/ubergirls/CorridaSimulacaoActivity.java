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

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class CorridaSimulacaoActivity extends AppCompatActivity {

    // Elementos da UI
    private ImageView imagemCarro;
    private TextView textoStatus;
    private ProgressBar progressoViagem;
    private TextView textoMotorista;
    private TextView textoCarro;
    private TextView textoTempo;

    // Handler para gerenciar atualizações com atraso
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Para controlar o estado da simulação
    private int etapaAtual = 0;
    private final int TOTAL_ETAPAS = 4;

    // Duração das animações (em ms)
    private static final int DURACAO_ANIMACAO_CARRO = 1500;
    private static final int DURACAO_TOTAL_VIAGEM = 60000; // 60 segundos para simulação completa

    // Arrays com mensagens para cada etapa
    private final String[] statusMensagens = {
            "Buscando Motorista",
            "Motorista a caminho",
            "Você está a caminho",
            "Chegando ao destino",
            "Viagem finalizada"
    };

    // Informações da viagem da tela anterior (EscolhaRota)
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

        // Buscar os TextViews do LinearLayout de informações adicionais
        // Considerando que o XML tenha sido atualizado com IDs para esses elementos
        textoMotorista = findViewById(R.id.textoMotorista);
        textoCarro = findViewById(R.id.textoCarro);
        textoTempo = findViewById(R.id.textoTempo);

        // Configuração inicial da barra de progresso
        progressoViagem.setIndeterminate(true);
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
            // Valores padrão caso não tenha recebido dados
            enderecoOrigem = "Origem não especificada";
            enderecoDestino = "Destino não especificado";
            tempoEstimado = 12;
            distanciaEstimada = 5.0;
            rotaSelecionada = 0;
        }
    }

    private void iniciarSimulacao() {
        // Iniciar as animações do carro
        iniciarAnimacaoCarro();

        // Iniciar o fluxo de atualizações de status
        atualizarStatusViagem(0);

        // Após 5 segundos, mudar para o próximo estágio (motorista a caminho)
        handler.postDelayed(() -> atualizarStatusViagem(1), 5000);

        // Após 15 segundos, mudar para o próximo estágio (viagem iniciada)
        handler.postDelayed(() -> {
            atualizarStatusViagem(2);

            // Mudar para barra de progresso determinada
            progressoViagem.setIndeterminate(false);
            progressoViagem.setMax(100);
            progressoViagem.setProgress(0);

            // Iniciar animação da barra de progresso
            iniciarAnimacaoProgresso();

        }, 15000);

        // Após 45 segundos, atualizar para "Chegando ao destino"
        handler.postDelayed(() -> atualizarStatusViagem(3), 45000);

        // Após 60 segundos, finalizar a viagem
        handler.postDelayed(() -> atualizarStatusViagem(4), DURACAO_TOTAL_VIAGEM);
    }

    private void iniciarAnimacaoCarro() {
        // Criar animação de movimento lateral (pequena oscilação para simular o carro em movimento)
        ObjectAnimator animX = ObjectAnimator.ofFloat(imagemCarro, "translationX", -10f, 10f);
        animX.setDuration(300);
        animX.setRepeatCount(ValueAnimator.INFINITE);
        animX.setRepeatMode(ValueAnimator.REVERSE);
        animX.setInterpolator(new AccelerateDecelerateInterpolator());

        // Criar animação de movimento vertical (simulando movimento na estrada)
        ObjectAnimator animY = ObjectAnimator.ofFloat(imagemCarro, "translationY", -5f, 5f);
        animY.setDuration(500);
        animY.setRepeatCount(ValueAnimator.INFINITE);
        animY.setRepeatMode(ValueAnimator.REVERSE);

        // Criar pequena animação de rotação para simular pequenas curvas
        ObjectAnimator rotation = ObjectAnimator.ofFloat(imagemCarro, "rotation", -2f, 2f);
        rotation.setDuration(800);
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.setRepeatMode(ValueAnimator.REVERSE);

        // Combinar as animações
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animX, animY, rotation);
        animatorSet.start();

        // Adicionar animação ocasional de "solavanco" para simular buracos na estrada
        adicionarAnimacaoSolavancos();
    }

    private void adicionarAnimacaoSolavancos() {
        // Executar a cada 3-8 segundos
        int delayAleatorio = 3000 + new Random().nextInt(5000);

        handler.postDelayed(() -> {
            // Criar animação de solavanco
            ObjectAnimator solavanco = ObjectAnimator.ofFloat(
                    imagemCarro, "translationY", 0f, -15f, 0f);
            solavanco.setDuration(400);
            solavanco.setInterpolator(new AccelerateDecelerateInterpolator());
            solavanco.start();

            // Adicionar o próximo solavanco
            adicionarAnimacaoSolavancos();

        }, delayAleatorio);
    }

    private void iniciarAnimacaoProgresso() {
        // Calcular quanto tempo ainda falta (já que a viagem já começou)
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
        // Calcular tempo restante baseado no progresso
        int tempoRestoMin = (int) ((100 - progresso) * tempoEstimado / 100);
        int tempoRestoSeg = (int) (((100 - progresso) * tempoEstimado * 60 / 100) % 60);

        // Formatar e exibir o tempo restante
        String textoTempoRestante = String.format("Tempo estimado: %d min %02d s",
                tempoRestoMin, tempoRestoSeg);
        if (textoTempo != null) {
            textoTempo.setText(textoTempoRestante);
        }
    }

    private void atualizarStatusViagem(int etapa) {
        etapaAtual = etapa;

        // Atualizar texto de status
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

        // Atualizar informações adicionais conforme a etapa
        atualizarInformacoesAdicionais(etapa);
    }

    private void atualizarInformacoesAdicionais(int etapa) {
        // Array de nomes de motoristas possíveis
        String[] nomes = {"Ana Silva", "Julia Santos", "Carla Oliveira", "Beatriz Lima", "Daniela Costa"};

        // Array de modelos de carros possíveis
        String[] modelos = {"Fiat Mobi - Rosa", "VW Up - Lilás", "Renault Kwid - Rosa", "Toyota Etios - Rosa"};

        // Array de placas possíveis
        String[] placas = {"ABC1234", "UBG5678", "GRL9012", "FEM3456", "SEG7890"};

        // Selecionar aleatoriamente um motorista se for a primeira etapa
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

        // Quando o motorista está a caminho, mostrar informação atualizada
        if (etapa == 1) {
            if (textoTempo != null) {
                textoTempo.setText("Chegada em: 3 min");
            }
        }
    }

    private void finalizarViagem() {
        // Parar as animações
        if (imagemCarro != null) {
            imagemCarro.clearAnimation();
        }

        // Atualizar progresso para 100%
        if (progressoViagem != null) {
            progressoViagem.setProgress(100);
            progressoViagem.setIndeterminate(false);
        }

        // Atualizar informações
        if (textoStatus != null) {
            textoStatus.setText("Viagem finalizada");
        }

        if (textoTempo != null) {
            textoTempo.setText("Tempo de viagem: " + tempoEstimado + " min");
        }

        // Após 3 segundos, redirecionar para a tela inicial (HomeActivity)
        handler.postDelayed(() -> {
            // Ir para HomeActivity
            Intent intent = new Intent(CorridaSimulacaoActivity.this, TelaHomeActivity.class);
            // FLAG_ACTIVITY_CLEAR_TOP limpa todas as activities acima da HomeActivity na pilha
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Finaliza esta Activity
        }, 3000);
    }
}