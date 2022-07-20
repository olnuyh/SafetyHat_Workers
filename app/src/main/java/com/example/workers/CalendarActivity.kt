package com.example.workers

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityCalendarBinding
import org.json.JSONArray
import org.json.JSONObject


class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calendarBtn.setOnClickListener {
            // Volley를 이용한 http 통신
            val calendaruploadRequest = object : StringRequest(
                Request.Method.POST,
                BuildConfig.API_KEY + "calendar_get.php",
                Response.Listener<String>{ response ->
                    if(response.toString().equals("-1")){ // 실패
                        Toast.makeText(this, "실패", Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(this, response, Toast.LENGTH_LONG).show()


                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                }){
                //override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                //val params : MutableMap<String, String> = HashMap()
                //params["work"] = work
                //return params
                //}
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(calendaruploadRequest)
        }
    }
}