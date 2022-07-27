package com.example.workers

import android.os.Bundle
import android.util.Log
import android.widget.CalendarView.OnDateChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityCalendarBinding
import org.json.JSONObject

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding :ActivityCalendarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            binding.calndarTest.text = String.format("%d-%d-%d", year, month+1, dayOfMonth)
//            val year = year.toString()
//            val month = month.toString()
//            val day = dayOfMonth.toString()
              val date=year.toString()+"="+month.toString()+"="+dayOfMonth.toString()

            // Volley를 이용한 http 통신
            val calendaruploadRequest = object : StringRequest(
                Request.Method.POST,
                BuildConfig.API_KEY + "calendar_get.php",
                Response.Listener<String>{ response ->
                    if(response.toString().equals("-1")){ // 실패
                        Toast.makeText(this, "실패", Toast.LENGTH_LONG).show()
                    }
                    else{
                        //Toast.makeText(this, response, Toast.LENGTH_LONG).show()

                        val jsonObject : JSONObject = JSONObject(response)
                        val Array = jsonObject.getJSONArray("webnautes")
                        for (i in 0 until Array.length()) {
                            val Object = Array.getJSONObject(i)
                            Log.d("--  work", Object.getString("work"))
                            Log.d("--  startdate", Object.getString("startdate"))
                            binding.calendarTv1.setText(Array.getJSONObject(0).getString("work"))
                            binding.calendarTv2.setText(Array.getJSONObject(1).getString("work"))

                        }


                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                }){
                override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                val params : MutableMap<String, String> = HashMap()
//                    params["year"] = year
//                    params["month"] = month
//                    params["day"] = day
                    params["date"] = date

                return params
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(calendaruploadRequest)
        }

        }

}