package com.example.n1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaterProgressView extends View {

    private static final String TITULO = "Consumo diário de água";
    private static final int COR_ALERTA = Color.parseColor("#FF5722");
    private static final int COR_FUNDO = Color.parseColor("#E3F2FD");

    private int progressColor;
    private int textColor;
    private int maxValue;

    private int consumoMl = 0;
    private float percentualAtual = 0;
    private boolean modoPorcentagem = true;

    private Paint paintFundo;
    private Paint paintProgresso;
    private Paint paintTexto;
    private ValueAnimator animacao;

    public WaterProgressView(Context context) {
        super(context);
        inicializar(context, null);
    }

    public WaterProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inicializar(context, attrs);
    }

    private void inicializar(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WaterProgressView);
        try {
            progressColor = ta.getColor(R.styleable.WaterProgressView_progressColor, Color.parseColor("#2196F3"));
            textColor = ta.getColor(R.styleable.WaterProgressView_textColor, Color.DKGRAY);
            maxValue = ta.getInt(R.styleable.WaterProgressView_maxValue, 2000);
        } finally {
            ta.recycle();
        }

        if (maxValue <= 0) {
            maxValue = 2000;
        }

        paintFundo = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFundo.setColor(COR_FUNDO);

        paintProgresso = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTexto.setTextAlign(Paint.Align.CENTER);

        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                modoPorcentagem = !modoPorcentagem;
                invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float largura = getWidth();
        float altura = getHeight();
        float base = Math.min(largura, altura);
        float centroX = largura / 2f;

        boolean passouDaMeta = consumoMl > maxValue;

        if (passouDaMeta) {
            paintProgresso.setColor(COR_ALERTA);
        } else {
            paintProgresso.setColor(progressColor);
        }

        paintTexto.setColor(textColor);
        paintTexto.setTextSize(base * 0.08f);
        canvas.drawText(TITULO, centroX, altura * 0.15f, paintTexto);

        String texto;
        if (modoPorcentagem) {
            texto = Math.round(percentualAtual) + "%";
            paintTexto.setTextSize(base * 0.22f);
        } else {
            texto = consumoMl + "/" + maxValue + " ml";
            paintTexto.setTextSize(base * 0.12f);
        }
        if (passouDaMeta) {
            paintTexto.setColor(COR_ALERTA);
        }
        canvas.drawText(texto, centroX, altura * 0.48f, paintTexto);

        float topoBarra = altura * 0.60f;
        float baseBarra = altura * 0.75f;
        float raio = (baseBarra - topoBarra) / 2f;

        float larguraProgresso = largura * Math.min(percentualAtual, 100f) / 100f;

        canvas.drawRoundRect(0, topoBarra, largura, baseBarra, raio, raio, paintFundo);
        canvas.drawRoundRect(0, topoBarra, larguraProgresso, baseBarra, raio, raio, paintProgresso);

        if (passouDaMeta) {
            paintTexto.setTextSize(base * 0.08f);
            canvas.drawText("Meta ultrapassada!", centroX, altura * 0.92f, paintTexto);
        }
    }

    public void setProgress(int percentual) {
        if (percentual < 0) {
            percentual = 0;
        }

        if (animacao != null) {
            animacao.cancel();
        }

        animacao = ValueAnimator.ofFloat(percentualAtual, percentual);
        animacao.setDuration(600);
        animacao.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                percentualAtual = (float) animator.getAnimatedValue();
                invalidate();
            }
        });
        animacao.start();
    }

    public void setConsumo(int valorMl) {
        if (valorMl < 0) {
            valorMl = 0;
        }
        this.consumoMl = valorMl;
        invalidate();
    }

    public void setMaxValue(int metaMl) {
        if (metaMl > 0) {
            this.maxValue = metaMl;
            invalidate();
        }
    }

    public int getMaxValue() {
        return maxValue;
    }
}
