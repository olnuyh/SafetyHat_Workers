package com.example.workers

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workers.databinding.ItemSalaryBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat

class SalaryViewHolder(val binding : ItemSalaryBinding) : RecyclerView.ViewHolder(binding.root)
class SalaryAdapter (val context : Context, val arr : JSONArray) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return arr.length() ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SalaryViewHolder(
            ItemSalaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as SalaryViewHolder).binding

        val salary = arr[position] as JSONObject

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(salary.getString("date"))

        binding.itemDate.text = SimpleDateFormat("MM월 dd일").format(date)
        binding.itemTime.text = (salary.getString("time").toInt() / 60).toString() + "시간"
        binding.itemSum.text = salary.getString("salary") + "원"
    }
}