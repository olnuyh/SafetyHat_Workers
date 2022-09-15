package com.example.workers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivitySalaryBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SalaryActivity : AppCompatActivity() {
    lateinit var binding : ActivitySalaryBinding
    var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySalaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val month = SimpleDateFormat("M", Locale.KOREA).format(Date())

        requestSalary(month)
        binding.salaryRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        var month_number = month.toInt()

        binding.salaryTitle.text = MyApplication.prefs.getString("worker_name", "") + " 님의 "+ month + "월 급여"

        binding.salaryPrevBtn.setOnClickListener {
            month_number = month_number - 1
            if(month_number == 0){
                month_number = 12
            }
            requestSalary(month_number.toString())
            binding.salaryTitle.text = MyApplication.prefs.getString("worker_name", "") + " 님의 " + month_number.toString()+ "월 급여"
        }

        binding.salaryNextBtn.setOnClickListener {
            month_number = month_number + 1
            if(month_number == 13){
                month_number = 1
            }
            requestSalary(month_number.toString())
            binding.salaryTitle.text = MyApplication.prefs.getString("worker_name", "") + " 님의 " + month_number.toString() + "월 급여"
        }

        binding.upDownBtn.setOnClickListener {
            if(count == 0){
                binding.salaryRecyclerView.visibility = View.VISIBLE
                count = 1

                binding.upDownBtn.setBackgroundResource(R.drawable.notification_up)
            }else if(count == 1){
                binding.salaryRecyclerView.visibility = View.GONE
                count = 0

                binding.upDownBtn.setBackgroundResource(R.drawable.notification_down)
            }
        }
    }

    fun requestSalary(month : String){
        val salaryRequest = object : StringRequest( // Volley를 이용한 http 통신
            Request.Method.POST,
            BuildConfig.API_KEY + "worker_salary.php",
            Response.Listener<String> { response ->
                val jsonObject : JSONObject = JSONObject(response)
                val array = jsonObject.getJSONArray("response")

                val time_sum = array.getJSONObject(0).getString("time_sum")

                if(time_sum.equals("null")){
                    binding.workTime.text = "0시간"
                    binding.workSalary.text = "0원"
                }else {
                    val time_sum = array.getJSONObject(0).getString("time_sum")

                    binding.workTime.text = (time_sum.toInt() / 60).toString() + "시간"
                    binding.workSalary.text = array.getJSONObject(0).getString("salary_sum") + "원"
                }

                array.remove(0)

                binding.salaryRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.salaryRecyclerView.adapter = SalaryAdapter(this, array)

            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                val params : MutableMap<String, String> = HashMap()
                params["month"] = month
                params["pkey"] = MyApplication.prefs.getString("worker_pkey", "")
                return params
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(salaryRequest)
    }
}