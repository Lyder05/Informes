package com.example.generarinformes

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var spCiudad: Spinner
    private lateinit var btnInforme: Button
    private lateinit var btnExportarPdf: Button

    private lateinit var llInforme: LinearLayout
    private lateinit var llOfertas: LinearLayout

    private lateinit var txtCiudadSeleccionada: TextView
    private lateinit var txtNumeroOfertas: TextView
    private lateinit var txtSalarioTotal: TextView
    private lateinit var txtSalarioMedio: TextView
    private lateinit var txtSalarioMaximo: TextView

    private lateinit var pbValencia: ProgressBar
    private lateinit var pbMadrid: ProgressBar
    private lateinit var pbAlicante: ProgressBar

    private lateinit var txtCantidadValencia: TextView
    private lateinit var txtCantidadMadrid: TextView
    private lateinit var txtCantidadAlicante: TextView

    private val ofertas = ArrayList<Oferta>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spCiudad = findViewById(R.id.spCiudad)
        btnInforme = findViewById(R.id.btnInforme)
        btnExportarPdf = findViewById(R.id.btnExportarPdf)

        llInforme = findViewById(R.id.llInforme)
        llOfertas = findViewById(R.id.llOfertas)

        txtCiudadSeleccionada = findViewById(R.id.txtCiudadSeleccionada)
        txtNumeroOfertas = findViewById(R.id.txtNumeroOfertas)
        txtSalarioTotal = findViewById(R.id.txtSalarioTotal)
        txtSalarioMedio = findViewById(R.id.txtSalarioMedio)
        txtSalarioMaximo = findViewById(R.id.txtSalarioMaximo)

        pbValencia = findViewById(R.id.pbValencia)
        pbMadrid = findViewById(R.id.pbMadrid)
        pbAlicante = findViewById(R.id.pbAlicante)

        txtCantidadValencia = findViewById(R.id.txtCantidadValencia)
        txtCantidadMadrid = findViewById(R.id.txtCantidadMadrid)
        txtCantidadAlicante = findViewById(R.id.txtCantidadAlicante)

        configurarSpinner()

        ofertas.addAll(cargarOfertas())
        
    }

    private fun configurarSpinner() {
        val ciudades = arrayOf("Todas", "Valencia", "Madrid", "Alicante")

        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            ciudades
        )

        adaptador.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spCiudad.adapter = adaptador
    }

    private fun cargarOfertas(): ArrayList<Oferta> {

        val listaOfertas = ArrayList<Oferta>()

        val textoJson = assets.open("ofertas.json")
            .bufferedReader()
            .use { it.readText() }

        val arrayJson = JSONArray(textoJson)

        for (i in 0 until arrayJson.length()) {

            val objetoJson = arrayJson.getJSONObject(i)

            val oferta = Oferta(
                objetoJson.getInt("id"),
                objetoJson.getString("puesto"),
                objetoJson.getString("empresa"),
                objetoJson.getString("ciudad"),
                objetoJson.getInt("salario")
            )

            listaOfertas.add(oferta)
        }

        return listaOfertas
    }
}
