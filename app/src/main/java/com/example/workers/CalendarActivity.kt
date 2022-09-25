package com.example.workers

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityCalendarBinding
import org.json.JSONObject


class CalendarActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle

    private lateinit var binding :ActivityCalendarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toggle.syncState()

        binding.mainDrawerView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.menuQr -> {
                    val intent = Intent(this, QrActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuNotification -> {
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuCalendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuSos -> {
                    val intent = Intent(this, SosActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuSalary -> {
                    val intent = Intent(this, SalaryActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        binding.logout.setOnClickListener {
            MyApplication.prefs.clear()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val date = year.toString()+"-"+(month + 1).toString()+"-"+dayOfMonth.toString()

            // Volley를 이용한 http 통신
            val calendaruploadRequest = object : StringRequest(
                Request.Method.POST,
                BuildConfig.API_KEY + "read_calendar.php",
                Response.Listener<String>{ response ->

                    val jsonObject : JSONObject = JSONObject(response)
                    val array = jsonObject.getJSONArray("response")

                    if(array.length() == 0){
                        binding.scheduleLayout.visibility = View.GONE
                    }
                    else{
                        binding.scheduleLayout.removeAllViews()

                        for (i in 0 until array.length()) {
                            val textView = TextView(this)
                            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            layoutParams.setMargins(120, 40, 0, 0)
                            textView.layoutParams = layoutParams
                            textView.text = array.getJSONObject(i).getString("calendar_contents")
                            binding.scheduleLayout.addView(textView)

                            val drawable = resources.getDrawable(R.drawable.calendar_point)
                            val imageView= ImageView(this)
                            val layoutParams2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            layoutParams2.setMargins(25, -45, 0, 0)
                            imageView.layoutParams = layoutParams2
                            imageView.setImageDrawable(drawable)
                            binding.scheduleLayout.addView(imageView)

                        }
                        binding.scheduleLayout.visibility = View.VISIBLE
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                }){
                override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                    val params : MutableMap<String, String> = HashMap()
                    params["date"] = date

                    return params
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(calendaruploadRequest)
        }
     }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) return true

        return when (item.itemId) {
            R.id.action_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.home,menu)
        return true
    }
}