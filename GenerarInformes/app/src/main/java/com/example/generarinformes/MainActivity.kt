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
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import kotlin.math.min

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

    private lateinit var txtSalarioMinimo: TextView

    private lateinit var pbValencia: ProgressBar
    private lateinit var pbMadrid: ProgressBar
    private lateinit var pbAlicante: ProgressBar

    private lateinit var txtCantidadValencia: TextView
    private lateinit var txtCantidadMadrid: TextView
    private lateinit var txtCantidadAlicante: TextView

    private val codigoCrearPdf = 100
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
        txtSalarioMinimo = findViewById(R.id.txtSalarioMinimo)

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

        btnExportarPdf.setOnClickListener {

            val ciudadSeleccionada = spCiudad.selectedItem.toString()

            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)

            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)

            llInforme.post {
                abrirSelectorParaGuardarPdf()
            }
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

        val salarioMinimo = ofertasFiltradas.minOfOrNull {
            it.salario
        } ?: 0

        txtCiudadSeleccionada.text = "Ciudad seleccionada: $ciudad"
        txtNumeroOfertas.text = "Número de ofertas: $numeroOfertas"
        txtSalarioTotal.text = "Salario total: ${formatearSalario(salarioTotal)}"
        txtSalarioMedio.text = "Salario medio: ${formatearSalario(salarioMedio.toInt())}"
        txtSalarioMaximo.text = "Salario máximo: ${formatearSalario(salarioMaximo)}"
        txtSalarioMinimo.text = "Salario mínimo: ${formatearSalario(salarioMinimo)}"

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

    private fun abrirSelectorParaGuardarPdf() {

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"

        intent.putExtra(
            Intent.EXTRA_TITLE,
            "informe_ofertas.pdf"
        )

        startActivityForResult(intent, codigoCrearPdf)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == codigoCrearPdf && resultCode == RESULT_OK) {

            val uriPdf = data?.data

            if (uriPdf != null) {
                exportarInformePdf(uriPdf)
            }
        }
    }



    private fun exportarInformePdf(uriPdf: Uri) {

        val documentoPdf = PdfDocument()

        val pagina = PdfDocument.PageInfo.Builder(
            595,
            842,
            1
        ).create()

        val paginaPdf = documentoPdf.startPage(pagina)
        val canvas = paginaPdf.canvas

        val pinturaTitulo = Paint()
        pinturaTitulo.color = Color.BLACK
        pinturaTitulo.textSize = 24f
        pinturaTitulo.isFakeBoldText = true

        canvas.drawColor(Color.WHITE)

        canvas.drawText(
            "Informe de ofertas",
            32f,
            48f,
            pinturaTitulo
        )

        val anchoDisponible = 531f
        val altoDisponible = 760f

        val anchoInforme = llInforme.width
        val altoInforme = llInforme.height

        if (anchoInforme > 0 && altoInforme > 0) {

            val escala = min(
                anchoDisponible / anchoInforme,
                altoDisponible / altoInforme
            )

            canvas.save()
            canvas.translate(32f, 72f)
            canvas.scale(escala, escala)

            llInforme.draw(canvas)

            canvas.restore()
        }

        documentoPdf.finishPage(paginaPdf)

        try {
            val salida = contentResolver.openOutputStream(uriPdf)

            if (salida != null) {

                salida.use {
                    documentoPdf.writeTo(it)
                }

                Toast.makeText(
                    this,
                    "PDF guardado",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                Toast.makeText(
                    this,
                    "No se pudo guardar el PDF",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "No se pudo guardar el PDF",
                Toast.LENGTH_LONG
            ).show()

        } finally {
            documentoPdf.close()
        }
    }



}
