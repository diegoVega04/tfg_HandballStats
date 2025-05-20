package es.diego.handballstats.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.diego.handballstats.R
import es.diego.handballstats.databinding.CardStatBinding
import es.diego.handballstats.models.Estadistica
import es.diego.handballstats.models.EstadisticaAtaque
import es.diego.handballstats.models.EstadisticaDefensa
import es.diego.handballstats.models.EstadisticaPortero

class ListaStatsAdapter(private var estadisticas: List<Estadistica>) : RecyclerView.Adapter<ListaStatsAdapter.ViewHolder>() {

    inner class ViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val binding = CardStatBinding.bind(vista)

        fun bind(stat:Estadistica) {
            when(stat){
                is EstadisticaAtaque -> {
                    binding.tipoStat.text = stat.tipo.toString()
                    binding.distancia.text = stat.distancia.toString()
                }

                is EstadisticaDefensa -> TODO()
                is EstadisticaPortero -> TODO()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_stat, parent, false)
        return ViewHolder(vista)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(estadisticas[position])
    }

    override fun getItemCount(): Int = estadisticas.size

    fun actualizarLista(nuevaLista: List<Estadistica>) {
        estadisticas = nuevaLista
        notifyDataSetChanged()
    }
}