package com.example.climaapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.Response.Listener
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.climaapp.adapter.PrevisaoAdapter
import com.example.climaapp.model.Clima
import com.example.climaapp.model.Previsao
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    val PERMISSION_ID = 42
    var lat: Double = 0.0
    var lon: Double = 0.0
    var listaPrevisoes = ArrayList<Previsao>()
    lateinit var dialog: ProgressDialog
    lateinit var url: String
    lateinit var queue: RequestQueue
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //<editor-fold desc="Gradiente Effect" defaultstate="collapsed">
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val animDrawable = root_layout.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(5000)
        animDrawable.start()
        //</editor-fold>

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        queue = Volley.newRequestQueue(this)

        dialog = ProgressDialog(this)
        dialog.setTitle("Trabalhando")
        dialog.setMessage("Recuperando informa????es do Clima, aguarde...")
        dialog.show()

        if (checkPermissions(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            getLastLocation()
        }
    }

    private fun requestWheater(url: String): StringRequest {

        dialog.show()
        var stringRequest = StringRequest(Request.Method.GET, url, Listener<String> { result ->
            val jsonResult = JSONObject(result).getJSONObject("results")
            val jsonPresisoesList = jsonResult.getJSONArray("forecast")

            val clima = preencheClima(jsonResult, listaPrevisoes)
            preenchePrevisoes(jsonPresisoesList)

            // Preencher os dados de Clina no UI
            textViewCidade.text = clima.nomeDaCidade
            textViewTemperatura.text = "${clima.temperatura.toString()}??"
            textViewHora.text = clima.hora
            textViewData.text = clima.data
            textViewMaxima.text = (clima.previsoes as ArrayList<Previsao>)[0].maxima
            textViewMinima.text = (clima.previsoes as ArrayList<Previsao>)[0].minima
            textViewTempoCelula.text = clima.descricao
            textViewNascerDoSol.text = clima.nascerDoSol
            textViewPorDoSol.text = clima.porDoSol
            textViewData.text =
                (clima.previsoes as ArrayList<Previsao>)[0].diaDaSemana?.toUpperCase()
                    .plus(" ").plus(clima.data)

            imageViewIcon.setImageResource(R.drawable.icon_snow)

            when (clima.condicaoDoTempo) {
                "storm" -> imageViewIcon.setImageResource(R.drawable.storm)
                "snow" -> imageViewIcon.setImageResource(R.drawable.snow)
                "rain" -> imageViewIcon.setImageResource(R.drawable.rain)
                "fog" -> imageViewIcon.setImageResource(R.drawable.fog)
                "clear_day" -> imageViewIcon.setImageResource(R.drawable.sun)
                "clear_night" -> imageViewIcon.setImageResource(R.drawable.moon)
                "cloud" -> imageViewIcon.setImageResource(R.drawable.cloudy)
                "cloudly_day" -> imageViewIcon.setImageResource(R.drawable.cloud_day)
                "cloudly_night" -> imageViewIcon.setImageResource(R.drawable.cloudy_night)
            }


            // Preencher ListView com a lista de Previsoes
            val adapter = PrevisaoAdapter(
                applicationContext,
                listaPrevisoes
            )
            listViewPrivisoes.adapter = adapter
            adapter.notifyDataSetChanged()

            dialog.dismiss()


            Log.d("RESPONSE: ", result.toString())
        }, Response.ErrorListener {
            Log.e("ERROR: ", it.localizedMessage)
        })

        queue.add(stringRequest)

        return stringRequest
    }

    private fun preenchePrevisoes(previsoes: JSONArray) {
        for (i in 0 until previsoes.length()) {
            val previsaoObject = previsoes.getJSONObject(i)
            val previsao = Previsao(
                previsaoObject.getString("date"),
                previsaoObject.getString("weekday"),
                previsaoObject.getString("max"),
                previsaoObject.getString("min"),
                previsaoObject.getString("description"),
                previsaoObject.getString("condition")
            )
            listaPrevisoes.add(previsao)
        }
    }

    private fun preencheClima(jsonObject: JSONObject, listaPrevisoes: ArrayList<Previsao>): Clima {
        val clima = Clima(
            jsonObject.getInt("temp"),
            jsonObject.getString("date"),
            jsonObject.getString("time"),
            jsonObject.getString("condition_code"),
            jsonObject.getString("description"),
            jsonObject.getString("currently"),
            jsonObject.getString("cid"),
            jsonObject.getString("city"),
            jsonObject.getString("img_id"),
            jsonObject.getInt("humidity"),
            jsonObject.getString("wind_speedy"),
            jsonObject.getString("sunrise"),
            jsonObject.getString("sunset"),
            jsonObject.getString("condition_slug"),
            jsonObject.getString("city_name")
        )
        clima.previsoes = listaPrevisoes
        return clima
    }

//    override fun onStart() {
//        super.onStart()
//        if (checkPermissions(
//                android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            )
//        ) {
//            getLastLocation()
//        }
//    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        /*
        Adi????o do listener/callback de sucesso ao obter a ??ltima localiza????o do dispositivo
         */
        mFusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
            if (location == null) {
                Log.e("LOCATION: ", "Erro ao obter Localizacao: ")
            } else {
                /*Aqui recebemos com sucesso a localiza????o utilizamo o m??todo apply(executa bloco de
                c??digo ao receber retorno do listener
                */
                location.apply {
                    // Escreve a localiza????o no LogCat, no tipo Debug[Ainda n??o vimos o Debug, por enquanto]
                    Log.d("LOCATION: ", location.toString())
                    //Adiciona as coordenadas ?? url de requisi????o
                    lat = location.latitude
                    lon = location.longitude

                    Log.d("LOCATION - LATITUDE: ",lat.toString())
                    Log.d("LOCATION - LONGITUDE: ",lon.toString())

                    url =
                        "https://api.hgbrasil.com/weather?key=4e56cf83&lat=${lat}&log=${lon}&user_ip=remote"

                    Log.d("GETLASTLOCATION: ", url)

                    //Chamada ?? requisi????o de Clima
                    requestWheater(url)
                }
            }
        }
    }

    private fun checkPermissions(vararg permission: String): Boolean {

        val mensagemPermissao = "A localiza????o ?? necess??ria para que possamos solicitar " +
                "a previs??o de clima em sua localidade."

        /*
        permission ?? um vararg, ou seja ele pode ser um argumeno, como podem ser varios argumentos,
            em nosso caso ele ir?? receber permiss??es para valid??-las uma a uma retornando um Boolean
         */
        val havePermission = permission.toList().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        /*
        Verifica se ha permissoes a solicitar, caso positivo ser?? solicitado ao usu??rio a permiss??o
        Se a permiss??o j?? foi negada, a solicita????o ser?? do tipo RequestPermissionRationale, ou seja
        uma expli????o mais sugestiva e explicativa deve ser solicitada ao usu??rio justificando o uso
        de sua localiza????o.
         */
        if (!havePermission) {
            /*
            Este trecho ?? executado quando a permiss??o j?? foi negada, ?? aqui que devemos
            convencer o usuario da necessidade da permiss??o para localiza????o do dispositivo.
            */
            if (permission.toList().any {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, it) }) {

                // Alerta justificando o uso da localiza????o.
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Permission")
                    .setMessage(mensagemPermissao)
                    .setPositiveButton("Ok") { id, v ->
                        run {
                            ActivityCompat.requestPermissions(this, permission, PERMISSION_ID)
                        }
                    }
                    .setNegativeButton("No") { id, v -> }
                    .create()
                alertDialog.show()
            } else {
                //Na primeira execu????o do app, esta solicita????o ?? executada
                ActivityCompat.requestPermissions(this, permission, PERMISSION_ID)
            }
            return false
        }
        return true
    }


    /*
    Ap??s o usu??rio autorizar ou negar a permiss??o o m??todo onRequestPermissionsResult ?? executado e
    todas as permiss??es passam por aqui, entao devevos selecionar qual ?? a permiss??o para poder
    executar as a????es necess??rias.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_ID -> {
                Log.d("PERMISSION", " - Concedida")
                getLastLocation()
            }
            else -> Log.d("PERMISSION: ", " - Negada")
        }
    }
}
