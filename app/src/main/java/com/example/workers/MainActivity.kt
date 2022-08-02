package com.example.workers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityMainBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lateinit var name : String
        var status by Delegates.notNull<Int>()

        val today = SimpleDateFormat("yyyy년 M월 d일").format(System.currentTimeMillis())

        binding.mainEmplId.text = MyApplication.prefs.getString("worker_id", "")
        binding.workBtn.isEnabled = false
        binding.leaveBtn.isEnabled = false

        val mainRequest = object : StringRequest(
            Request.Method.POST,
            BuildConfig.API_KEY + "read_information.php",
            Response.Listener<String>{ response ->

                val jsonObject : JSONObject = JSONObject(response)
                val array = jsonObject.getJSONArray("response")

                name = array.getJSONObject(0).getString("name")
                binding.mainName.text = name
                binding.mainContents.text = name + " 님은 " + today + "구역에서 까지 근무입니다."

                status = array.getJSONObject(0).getString("status").toInt()

                if(status == 0){
                    binding.workBtn.isEnabled = true
                }
                else{
                    binding.leaveBtn.isEnabled = true
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                val params : MutableMap<String, String> = HashMap()
                params["pkey"] = MyApplication.prefs.getString("worker_pkey", "")

                return params
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(mainRequest)

        binding.workBtn.setOnClickListener {
            val intent = Intent(this, QrActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("status", status)
            startActivity(intent)
            finish()
        }

        binding.leaveBtn.setOnClickListener {
            val intent = Intent(this, QrActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("status", status)
            startActivity(intent)
            finish()
        }

        binding.logoutBtn.setOnClickListener {
            MyApplication.prefs.clear()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
