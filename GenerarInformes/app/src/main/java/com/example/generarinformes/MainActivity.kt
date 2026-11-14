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

        btnInforme.setOnClickListener {

            val ciudadSeleccionada = spCiudad.selectedItem.toString()

            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)

            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)
        }

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

    private fun filtrarOfertasPorCiudad(ciudad: String): List<Oferta> {

        val resultado = if (ciudad == "Todas") {
            ofertas
        } else {
            ofertas.filter {
                it.ciudad == ciudad
            }
        }

        return resultado
    }


    private fun mostrarResultado(
        ciudad: String,
        ofertasFiltradas: List<Oferta>
    ) {
        val numeroOfertas = ofertasFiltradas.size

        val salarioTotal = ofertasFiltradas.sumOf {
            it.salario
        }

        val salarioMedio = if (ofertasFiltradas.isNotEmpty()) {
            ofertasFiltradas.map { it.salario }.average()
        } else {
            0.0
        }

        val salarioMaximo = ofertasFiltradas.maxOfOrNull {
            it.salario
        } ?: 0

        txtCiudadSeleccionada.text = "Ciudad seleccionada: $ciudad"
        txtNumeroOfertas.text = "Número de ofertas: $numeroOfertas"
        txtSalarioTotal.text = "Salario total: ${formatearSalario(salarioTotal)}"
        txtSalarioMedio.text = "Salario medio: ${formatearSalario(salarioMedio.toInt())}"
        txtSalarioMaximo.text = "Salario máximo: ${formatearSalario(salarioMaximo)}"

        mostrarGrafico(ofertasFiltradas)

        llOfertas.removeAllViews()

        ofertasFiltradas.forEachIndexed { indice, oferta ->

            val txtOferta = TextView(this)

            txtOferta.text = "${indice + 1}. ${oferta.puesto}\n" +
                    "${oferta.empresa} · ${oferta.ciudad} · " +
                    formatearSalario(oferta.salario)

            txtOferta.textSize = 16f
            txtOferta.setPadding(0, 0, 0, 24)

            llOfertas.addView(txtOferta)
        }
    }

    private fun formatearSalario(salario: Int): String {
        return "%,d".format(salario).replace(",", ".") + " €"
    }


    private fun mostrarGrafico(ofertasFiltradas: List<Oferta>) {

        val ofertasValencia = ofertasFiltradas.count {
            it.ciudad == "Valencia"
        }

        val ofertasMadrid = ofertasFiltradas.count {
            it.ciudad == "Madrid"
        }

        val ofertasAlicante = ofertasFiltradas.count {
            it.ciudad == "Alicante"
        }

        val valorMaximo = maxOf(
            ofertasValencia,
            ofertasMadrid,
            ofertasAlicante,
            1
        )

        pbValencia.max = valorMaximo
        pbMadrid.max = valorMaximo
        pbAlicante.max = valorMaximo

        pbValencia.progress = ofertasValencia
        pbMadrid.progress = ofertasMadrid
        pbAlicante.progress = ofertasAlicante

        txtCantidadValencia.text = ofertasValencia.toString()
        txtCantidadMadrid.text = ofertasMadrid.toString()
        txtCantidadAlicante.text = ofertasAlicante.toString()
    }


}
