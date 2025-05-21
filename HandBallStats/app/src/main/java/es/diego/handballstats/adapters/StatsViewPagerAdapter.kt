package es.diego.handballstats.fragments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import es.diego.handballstats.activities.JugadorActivity

class StatsViewPagerAdapter(activity: JugadorActivity): FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AtaqueFragment()
            1 -> DefensaFragment()
            else -> throw IllegalStateException("Posicion desconocida")
        }
    }
}