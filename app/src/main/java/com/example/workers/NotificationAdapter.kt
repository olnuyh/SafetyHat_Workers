package com.example.workers

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workers.databinding.ItemNotificationBinding
import org.json.JSONArray
import org.json.JSONObject

class NotificationViewHolder(val binding : ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
class NotificationAdapter(val context : Context, val arr : JSONArray) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 선택 데이터 리스트
    private var selectedItems: SparseBooleanArray = SparseBooleanArray()

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
        binding.itemContent.text = notification.getString("notification_contents")
        binding.itemName.text="관리자 "+notification.getString("notification_writer")
        val insert_date = notification.getString("insert_date")
        val date = insert_date.substring(0, insert_date.indexOf(" "))
        binding.itemDate.text=date

        binding.ivItemUpImg.setOnClickListener {
            if (selectedItems.get(position)) {
                // VISIBLE -> INVISIBLE
                selectedItems.delete(position)

                binding.clItemExpand.visibility = View.GONE
            }
            binding.ivItemUpImg.visibility= View.GONE
            binding.ivItemDownImg.visibility= View.VISIBLE
        }

        binding.ivItemDownImg.setOnClickListener {

            if (selectedItems.get(position)) {

            } else {
                // INVISIBLE -> VISIBLE
                selectedItems.put(position, true)

                binding.clItemExpand.visibility = View.VISIBLE
            }
            binding.ivItemDownImg.visibility= View.GONE
            binding.ivItemUpImg.visibility= View.VISIBLE
        }
    }
}