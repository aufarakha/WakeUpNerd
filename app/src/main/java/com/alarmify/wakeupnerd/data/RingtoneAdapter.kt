package com.alarmify.wakeupnerd.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alarmify.wakeupnerd.R
import com.google.android.material.button.MaterialButton

class RingtoneAdapter(
    private val ringtones: List<Ringtone>,
    private val onApplyClick: (Int) -> Unit
) : RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder>() {

    inner class RingtoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRingtoneName: TextView = itemView.findViewById(R.id.tvRingtoneName)
        val tvRingtoneDuration: TextView = itemView.findViewById(R.id.tvRingtoneDuration)
        val btnApply: MaterialButton = itemView.findViewById(R.id.btnApply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ringtone, parent, false)
        return RingtoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: RingtoneViewHolder, position: Int) {
        val ringtone = ringtones[position]

        holder.tvRingtoneName.text = ringtone.name
        holder.tvRingtoneDuration.text = ringtone.duration

        holder.btnApply.setOnClickListener {
            onApplyClick(position)
        }
    }

    override fun getItemCount() = ringtones.size
}
