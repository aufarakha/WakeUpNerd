package com.alarmify.wakeupnerd.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.alarmify.wakeupnerd.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

class AlarmAdapter(
    private val alarms: List<Alarm>,
    private val onSwitchChanged: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        val switchAlarm: MaterialSwitch = itemView.findViewById(R.id.switchAlarm)
        val cardAlarm: MaterialCardView = itemView.findViewById(R.id.cardAlarm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvTime.text = alarm.time
        holder.tvDesc.text = alarm.description
        holder.switchAlarm.isChecked = alarm.isEnabled

        // Set stroke for highlighted alarm
        if (alarm.isHighlighted) {
            holder.cardAlarm.strokeColor = 0xFF03A9F4.toInt()
            holder.cardAlarm.strokeWidth = 6
        } else {
            holder.cardAlarm.strokeWidth = 0
        }

        holder.switchAlarm.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChanged(position, isChecked)
        }
    }

    override fun getItemCount() = alarms.size
}
