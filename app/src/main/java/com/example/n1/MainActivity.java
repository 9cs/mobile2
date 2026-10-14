package com.example.n1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NOME = "hidratacao_prefs";
    private static final String CHAVE_META = "meta_diaria_ml";
    private static final String CHAVE_CONSUMO = "consumo_dia_ml";
    private static final String CHAVE_DATA = "data_ultimo_registro";
    private static final String CHAVE_REGISTROS = "registros_do_dia";

    private static final int VALOR_MINIMO = 1;
    private static final int VALOR_MAXIMO = 99999;

    private WaterProgressView waterProgressView;
    private TextView txtAlerta;
    private TextView txtAcumulado;
    private TextView txtRegistros;
    private EditText edtConsumo;
    private EditText edtMeta;
    private Button btnRegistrar;
    private Button btnDefinirMeta;

    private SharedPreferences prefs;
    private int metaDiaria;
    private int consumoDia;
    private String registros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waterProgressView = findViewById(R.id.waterProgressView);
        txtAlerta = findViewById(R.id.txtAlerta);
        txtAcumulado = findViewById(R.id.txtAcumulado);
        txtRegistros = findViewById(R.id.txtRegistros);
        edtConsumo = findViewById(R.id.edtConsumo);
        edtMeta = findViewById(R.id.edtMeta);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnDefinirMeta = findViewById(R.id.btnDefinirMeta);

        prefs = getSharedPreferences(PREFS_NOME, MODE_PRIVATE);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarConsumo();
            }
        });

        btnDefinirMeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                definirMeta();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDados();
        verificarNovoDia();
        atualizarTela();
    }

    private void carregarDados() {
        metaDiaria = prefs.getInt(CHAVE_META, waterProgressView.getMaxValue());
        consumoDia = prefs.getInt(CHAVE_CONSUMO, 0);
        registros = prefs.getString(CHAVE_REGISTROS, "");
    }

    private void salvarDados() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(CHAVE_META, metaDiaria);
        editor.putInt(CHAVE_CONSUMO, consumoDia);
        editor.putString(CHAVE_REGISTROS, registros);
        editor.putString(CHAVE_DATA, obterDataAtual());
        editor.apply();
    }

    private void verificarNovoDia() {
        String dataHoje = obterDataAtual();
        String dataSalva = prefs.getString(CHAVE_DATA, "");

        if (!dataHoje.equals(dataSalva)) {
            consumoDia = 0;
            registros = "";
            salvarDados();
        }
    }

    private String obterDataAtual() {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formato.format(new Date());
    }

    private String obterHoraAtual() {
        SimpleDateFormat formato = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formato.format(new Date());
    }

    private int lerValorMl(EditText campo) {
        String texto = campo.getText().toString().trim();

        if (texto.isEmpty()) {
            campo.setError("Informe um valor em ml");
            return -1;
        }

        int valor = Integer.parseInt(texto);

        if (valor < VALOR_MINIMO || valor > VALOR_MAXIMO) {
            campo.setError("Digite um valor entre " + VALOR_MINIMO + " e " + VALOR_MAXIMO + " ml");
            return -1;
        }

        return valor;
    }

    private void registrarConsumo() {
        int quantidade = lerValorMl(edtConsumo);
        if (quantidade == -1) {
            return;
        }

        verificarNovoDia();

        String novoRegistro = obterHoraAtual() + " - " + quantidade + " ml";
        if (registros.isEmpty()) {
            registros = novoRegistro;
        } else {
            registros = registros + "\n" + novoRegistro;
        }

        consumoDia = consumoDia + quantidade;
        salvarDados();
        atualizarTela();

        edtConsumo.setText("");
        Toast.makeText(this, "+" + quantidade + " ml registrados!", Toast.LENGTH_SHORT).show();
    }

    private void definirMeta() {
        int novaMeta = lerValorMl(edtMeta);
        if (novaMeta == -1) {
            return;
        }

        metaDiaria = novaMeta;
        salvarDados();
        atualizarTela();

        edtMeta.setText("");
        Toast.makeText(this, "Meta alterada para " + novaMeta + " ml", Toast.LENGTH_SHORT).show();
    }

    private void atualizarTela() {
        int percentual = consumoDia * 100 / metaDiaria;

        waterProgressView.setMaxValue(metaDiaria);
        waterProgressView.setConsumo(consumoDia);
        waterProgressView.setProgress(percentual);

        txtAcumulado.setText("Consumo acumulado: " + consumoDia + " ml de " + metaDiaria + " ml");

        if (registros.isEmpty()) {
            txtRegistros.setText("Nenhum registro hoje");
        } else {
            txtRegistros.setText(registros);
        }

        if (consumoDia > metaDiaria) {
            txtAlerta.setVisibility(View.VISIBLE);
        } else {
            txtAlerta.setVisibility(View.GONE);
        }
    }
}
