package es.diego.handballstats.adapters

import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.diego.handballstats.R
import es.diego.handballstats.services.Sesion
import es.diego.handballstats.services.ApiClient
import es.diego.handballstats.databinding.CardJugadorBinding
import es.diego.handballstats.models.Jugador
import kotlinx.coroutines.launch
import java.io.File

class JugadoresAdapter (
    private val jugadores: MutableList<Jugador>,
    private val lifeCycleOwner: LifecycleOwner
) : RecyclerView.Adapter<JugadoresAdapter.JugadoresViewHolder>() {

    inner class JugadoresViewHolder(val vista: View): RecyclerView.ViewHolder(vista) {

        val binding = CardJugadorBinding.bind(vista)

        fun bind(jugador: Jugador) {
            binding.jugadorNombreText.text = jugador.nombre
            binding.jugadorAnioText.text = jugador.anio.toString()
            binding.jugadorDorsalText.text = jugador.dorsal.toString()

            if (!jugador.foto.isNullOrEmpty()) {
                val fotoFile = File(jugador.foto!!)
                if (fotoFile.exists()) {
                    Glide.with(vista)
                        .load(fotoFile)
                        .circleCrop()
                        .into(binding.jugadorImageView)
                }
            }

            vista.setOnClickListener() {
                val dialogView = LayoutInflater.from(vista.context).inflate(R.layout.dialog_crear_jugador, null)
                val dialog = AlertDialog.Builder(vista.context)
                    .setView(dialogView)
                    .create()

                val nombreEditText = dialogView.findViewById<EditText>(R.id.playerNameEditText)
                val anioEditText = dialogView.findViewById<EditText>(R.id.playerBirthYearEditText)
                val dorsal = dialogView.findViewById<EditText>(R.id.playerDorsalEditText)
                val guardarButton = dialogView.findViewById<Button>(R.id.savePlayerButton)
                val eliminarButton = dialogView.findViewById<ImageButton>(R.id.eliminarButton)
                val fotoPerfil = dialogView.findViewById<ImageView>(R.id.playerPhotoImageView)

                guardarButton.setText("Actualizar datos")
                nombreEditText.text = Editable.Factory.getInstance().newEditable(jugador.nombre)
                anioEditText.text = Editable.Factory.getInstance().newEditable(jugador.anio.toString())
                dorsal.text = Editable.Factory.getInstance().newEditable(jugador.dorsal.toString())
                eliminarButton.visibility = View.VISIBLE

                if (!jugador.foto.isNullOrEmpty()) {
                    val fotoFile = File(jugador.foto!!)
                    if (fotoFile.exists()) {
                        Glide.with(vista)
                            .load(fotoFile)
                            .circleCrop()
                            .into(fotoPerfil)
                    }
                }

                eliminarButton.setOnClickListener(){
                    val dialog = AlertDialog.Builder(vista.context)
                        .setTitle("Eliminar jugador")
                        .setMessage("¿Seguro que desea borrar este jugador?")
                        .setCancelable(true)
                        .setPositiveButton("Eliminar jugador") { _, _ ->
                            val dialog = AlertDialog.Builder(vista.context)
                                .setCancelable(true)
                                .setTitle("Doble confimacion")
                                .setMessage("Esta operación no se puede cancelar, se eliminarán permanentemente el jugador y todas las estadísticas asociadas")
                                .setNegativeButton("Cancelar", null)
                                .setPositiveButton("Eliminar jugador") { _, _ ->
                                    dialog.dismiss()
                                    borrarJugador(jugador)
                                }
                                .create()

                            dialog.show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .create()

                    dialog.show()
                }

                guardarButton.setOnClickListener {
                    val nombre = nombreEditText.text.toString()
                    val anio = anioEditText.text
                    val dorsal = dorsal.text

                    if (nombre.isNotEmpty() && anio.isNotEmpty() && dorsal.isNotEmpty()) {
                        jugador.nombre = nombre
                        jugador.anio = anio.toString().toInt()
                        jugador.dorsal = dorsal.toString().toInt()
                        actualizarJugador(jugador)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(vista.context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.setCanceledOnTouchOutside(true)
                dialog.show()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): JugadoresViewHolder {
        return JugadoresViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_jugador, parent, false)
        )
    }

    override fun onBindViewHolder(holder: JugadoresViewHolder, position: Int) {
        holder.bind(jugadores[position])
    }

    override fun getItemCount(): Int = jugadores.size

    private fun agregarJugador(a: Jugador) {
        jugadores.add(a)
        notifyDataSetChanged()
    }

    private fun borrarJugador(jugador: Jugador) {
        lifeCycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.jugadorService.borrarJugador(Sesion.token!!, jugador.id!!)
                if (response.isSuccessful) {
                    jugadores.remove(jugador)

                    //eliminar la foto del jugador del almacenamiento
                    jugador.foto?.let { fotoPath ->
                        val file = File(fotoPath)
                        if (file.exists()) {
                            val eliminado = file.delete()
                            if (eliminado) {
                                Log.d("EliminarFoto", "Foto eliminada correctamente.")
                            } else {
                                Log.e("EliminarFoto", "No se pudo eliminar la foto.")
                            }
                        } else {
                            Log.w("EliminarFoto", "Archivo no encontrado en la ruta: $fotoPath")
                        }
                        jugador.foto = null
                    } ?: run {
                        Log.w("EliminarFoto", "El jugador no tiene una foto asignada.")
                    }

                    notifyDataSetChanged()
                }
            } catch (e:Exception) {Log.e("ERROR", "error: ${e.message}")}
        }
    }

    private fun actualizarJugador(jugador: Jugador) {
        lifeCycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.jugadorService.actualizarJugador(Sesion.token!!, jugador)
                if (response.isSuccessful) {
                    notifyDataSetChanged()
                }
            } catch (e:Exception) {Log.e("AQUI", "error: ${e.message}")}
        }
    }
}