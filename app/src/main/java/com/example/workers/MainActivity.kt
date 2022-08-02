package com.example.workers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.text.SimpleDateFormat
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        binding.mainDrawerView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.menuNotification -> {
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuSalary -> {
                    val intent = Intent(this, SalaryActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuCalendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuMyPage -> {
                    val intent = Intent(this, MypageActivity::class.java)
                    startActivity(intent)
                }
                R.id.logout -> {
                    MyApplication.prefs.clear()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

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

                val navigationView: NavigationView =  findViewById(R.id.main_drawer_view)
                val headerView = navigationView.getHeaderView(0)
                val navName : TextView = headerView.findViewById(R.id.navigationName)
                val navEmplId : TextView = headerView.findViewById(R.id.navigationEmplId)
                navName.text = name
                navEmplId.text = MyApplication.prefs.getString("worker_id", "")
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) return true

        return super.onOptionsItemSelected(item)
    }
}
