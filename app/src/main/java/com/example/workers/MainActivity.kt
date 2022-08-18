package com.example.workers

import android.R.attr.bitmap
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.workers.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle

    private val GET_GALLERY_IMAGE = 200

    //var requestQueue: RequestQueue? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        //프로필 불러오기
        val readprofileRequest = object : StringRequest(
            Request.Method.POST,
            BuildConfig.API_KEY + "read_profile.php",
            Response.Listener<String>{ response ->
                Toast.makeText(this,response.toString(),Toast.LENGTH_LONG).show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }){

        }
        val queue2 = Volley.newRequestQueue(this)
        queue2.add(readprofileRequest)




//        if(requestQueue == null){
//            requestQueue = Volley.newRequestQueue(getApplicationContext());
//        }

        val request= object : StringRequest(
            Method.GET, BuildConfig.WEATHER_API_KEY,
            Response.Listener<String>{ response ->
                val jsonObject = JSONObject(response)
                val city = jsonObject.getString("name")
                binding.cityView.text=city

                val weatherJson = jsonObject.getJSONArray("weather")
                val weatherObj = weatherJson.getJSONObject(0)
                val weather = weatherObj.getString("description")

                val icon = weatherObj.getString("icon")

                val imageView = findViewById<ImageView>(R.id.weathericon)
                val imageUrl01d = "http://openweathermap.org/img/wn/01d@2x.png"
                val imageUrl02d = "http://openweathermap.org/img/wn/02d@2x.png"
                val imageUrl03d = "http://openweathermap.org/img/wn/03d@2x.png"
                val imageUrl04d = "http://openweathermap.org/img/wn/04d@2x.png"
                val imageUrl09d = "http://openweathermap.org/img/wn/09d@2x.png"
                val imageUrl10d = "http://openweathermap.org/img/wn/10d@2x.png"
                val imageUrl11d = "http://openweathermap.org/img/wn/11d@2x.png"
                val imageUrl13d = "http://openweathermap.org/img/wn/13d@2x.png"
                val imageUrl50d = "http://openweathermap.org/img/wn/50d@2x.png"
                val imageUrl01n = "http://openweathermap.org/img/wn/01n@2x.png"
                val imageUrl02n = "http://openweathermap.org/img/wn/02n@2x.png"
                val imageUrl03n = "http://openweathermap.org/img/wn/03n@2x.png"
                val imageUrl04n = "http://openweathermap.org/img/wn/04n@2x.png"
                val imageUrl09n = "http://openweathermap.org/img/wn/09n@2x.png"
                val imageUrl10n = "http://openweathermap.org/img/wn/10n@2x.png"
                val imageUrl11n = "http://openweathermap.org/img/wn/11n@2x.png"
                val imageUrl13n = "http://openweathermap.org/img/wn/13n@2x.png"
                val imageUrl50n = "http://openweathermap.org/img/wn/50n@2x.png"

                binding.weatherView.text=weather

                when (icon) {
                    "01d" -> Glide.with(this).load(imageUrl01d).into(imageView)
                    "02d" -> Glide.with(this).load(imageUrl02d).into(imageView)
                    "03d" -> Glide.with(this).load(imageUrl03d).into(imageView)
                    "04d" -> Glide.with(this).load(imageUrl04d).into(imageView)
                    "09d" -> Glide.with(this).load(imageUrl09d).into(imageView)
                    "10d" -> Glide.with(this).load(imageUrl10d).into(imageView)
                    "11d" -> Glide.with(this).load(imageUrl11d).into(imageView)
                    "13d" -> Glide.with(this).load(imageUrl13d).into(imageView)
                    "50d" -> Glide.with(this).load(imageUrl50d).into(imageView)
                    "01n" -> Glide.with(this).load(imageUrl01n).into(imageView)
                    "02n" -> Glide.with(this).load(imageUrl02n).into(imageView)
                    "03n" -> Glide.with(this).load(imageUrl03n).into(imageView)
                    "04n" -> Glide.with(this).load(imageUrl04n).into(imageView)
                    "09n" -> Glide.with(this).load(imageUrl09n).into(imageView)
                    "10n" -> Glide.with(this).load(imageUrl10n).into(imageView)
                    "11n" -> Glide.with(this).load(imageUrl11n).into(imageView)
                    "13n" -> Glide.with(this).load(imageUrl13n).into(imageView)
                    "50n" -> Glide.with(this).load(imageUrl50n).into(imageView)
                    else -> println("null")
                }


                val tempK = JSONObject(jsonObject.getString("main"))
                val tempDo = Math.round((tempK.getDouble("temp") - 273.15) * 100) / 100.0
                binding.tempView.text="$tempDo°C"


            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }){
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                return HashMap()
            }
        }

//        request.setShouldCache(false)
//        requestQueue!!.add(request)
        val setqueue = Volley.newRequestQueue(this)
        setqueue.add(request)


        val navView=binding.mainDrawerView
        val headerView=navView.getHeaderView(0)
        val editbtn=headerView.findViewById<Button>(R.id.navigationEditBtn)
        val camerabtn=headerView.findViewById<Button>(R.id.navigationCameraBtn)
        val savebtn=headerView.findViewById<Button>(R.id.navigationSaveBtn)

        editbtn.setOnClickListener {
            camerabtn.visibility=View.VISIBLE
            editbtn.visibility=View.GONE
            savebtn.visibility=View.VISIBLE
        }
        camerabtn.setOnClickListener{
//            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.setType("image/*")
//            startActivityForResult(intent,GALLERY)

            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, GET_GALLERY_IMAGE)
        }

        savebtn.setOnClickListener {
            uploadDataToDB()
        }


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




//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        val mainnavView = findViewById<NavigationView>(R.id.main_drawer_view)
//        val headerView2=mainnavView.getHeaderView(0)
//        val profile=headerView2.findViewById<ImageView>(R.id.navigationProfile)
//        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.data != null) {
//            val selectedImageUri = data.data
//            profile.setImageURI(selectedImageUri)
//        }
//    }

    var encodeImageString: String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val mainnavView = findViewById<NavigationView>(R.id.main_drawer_view)
        val headerView2=mainnavView.getHeaderView(0)
        val profile=headerView2.findViewById<ImageView>(R.id.navigationProfile)
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                profile.setImageBitmap(bitmap)
                encodeBitmapImage(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun encodeBitmapImage(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val bytesOfImage: ByteArray = byteArrayOutputStream.toByteArray()
        encodeImageString = Base64.encodeToString(bytesOfImage, Base64.DEFAULT)
    }

    private fun uploadDataToDB() {
        // Volley를 이용한 http 통신
        val writeprofileRequest = object : StringRequest(
            Request.Method.POST,
            BuildConfig.API_KEY + "write_profile.php",
            Response.Listener<String>{ response ->

            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                val params : MutableMap<String, String> = HashMap()
                params["profile"] = encodeImageString.toString()
                return params
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(writeprofileRequest)
    }



}
