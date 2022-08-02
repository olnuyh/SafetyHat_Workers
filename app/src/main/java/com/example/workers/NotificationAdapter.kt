package com.example.workers

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workers.databinding.ItemNotificationBinding
import org.json.JSONArray
import org.json.JSONObject

class NotificationViewHolder(val binding : ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
class NotificationAdapter(val context : Context, val arr : JSONArray) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return arr.length()?:0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NotificationViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as NotificationViewHolder).binding

        val notification = arr[position] as JSONObject
        binding.itemTitle.text = notification.getString("notification_title")
        val insert_date = notification.getString("insert_date")
        val date = insert_date.substring(0, insert_date.indexOf(" "))
        binding.itemDate.text = date
    }
}