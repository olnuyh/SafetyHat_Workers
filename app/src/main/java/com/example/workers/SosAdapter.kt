package com.example.workers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workers.databinding.ItemNotificationBinding
import com.example.workers.databinding.ItemSosBinding
import org.json.JSONObject
import java.text.SimpleDateFormat

class SosViewHolder(val binding : ItemSosBinding) : RecyclerView.ViewHolder(binding.root)
class SosAdapter(val context : Context, val arr : ArrayList<SosMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return arr.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SosViewHolder(ItemSosBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as SosViewHolder).binding

        val message = arr[position]

        val timestamp = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(message.timeStamp)

        binding.sosMessageDate.text = SimpleDateFormat("yy/MM/dd").format(timestamp)
        binding.sosMessageTime.text = SimpleDateFormat("a hh:mm").format(timestamp)
        binding.sosMessageContents.text = message.content
    }
}