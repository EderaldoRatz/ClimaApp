package com.example.climaapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.climaapp.R
import com.example.climaapp.model.Previsao

class PrevisaoAdapter(private val context: Context, private val dataSource: ArrayList<Previsao>): BaseAdapter() {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = LayoutInflater.from(context).inflate(R.layout.previsao_cell, null, true)
        val textViewTempo = rowView.findViewById<TextView>(R.id.textViewTempoCelula)
        val textViewMaxMin= rowView.findViewById<TextView>(R.id.textViewMaxMinCelula)
        val imageViewClima = rowView.findViewById<ImageView>(R.id.imageViewIconWeather)

        val previsao = getItem(position) as Previsao

        textViewTempo.text = previsao.diaDaSemana?.toUpperCase()
            .plus(" - ").plus(previsao.descricao)
        textViewMaxMin.text = previsao.data.plus(" Máx: ")
            .plus(previsao.maxima).plus("° Mín: ").plus(previsao.minima).plus("°")

        when(previsao.condicao){
            "storm" -> imageViewClima.setImageResource(R.drawable.storm)
            "snow" -> imageViewClima.setImageResource(R.drawable.snow)
            "rain" -> imageViewClima.setImageResource(R.drawable.rain)
            "fog" -> imageViewClima.setImageResource(R.drawable.fog)
            "clear_day" -> imageViewClima.setImageResource(R.drawable.sun)
            "clear_night" -> imageViewClima.setImageResource(R.drawable.moon)
            "cloud" -> imageViewClima.setImageResource(R.drawable.cloudy)
            "cloudly_day" -> imageViewClima.setImageResource(R.drawable.cloud_day)
            "cloudly_night" -> imageViewClima.setImageResource(R.drawable.cloudy_night)
        }

        return rowView
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }

}