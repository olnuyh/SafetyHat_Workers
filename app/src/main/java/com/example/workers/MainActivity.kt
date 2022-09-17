package com.example.workers

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.workers.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var binding : ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

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

        val navView = binding.mainDrawerView
        val headerView = navView.getHeaderView(0)

        val editbtn=headerView.findViewById<ImageButton>(R.id.navigationEditBtn)
        val camerabtn=headerView.findViewById<ImageButton>(R.id.navigationCameraBtn)
        val savebtn=headerView.findViewById<ImageButton>(R.id.navigationSaveBtn)
        val profileImage=headerView.findViewById<ImageView>(R.id.navigationProfile)
        val xbtn=headerView.findViewById<ImageButton>(R.id.navigationCancel)

        var encodeImageString: String? = null

        xbtn.setOnClickListener {
            binding.drawerLayout.closeDrawers()
        }

        editbtn.setOnClickListener {
            camerabtn.visibility=View.VISIBLE
            editbtn.visibility=View.GONE
            savebtn.visibility=View.VISIBLE
        }

        // 갤러리 연동
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){

            try{
                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
                inputStream = null

                val resizedBitmap = resize(bitmap)
                profileImage.setImageBitmap(resizedBitmap)

                //DB에 저장할 형태로 변경
                val byteArrayOutputStream = ByteArrayOutputStream()
                resizedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val bytesOfImage: ByteArray = byteArrayOutputStream.toByteArray()
                encodeImageString = Base64.encodeToString(bytesOfImage, Base64.DEFAULT)
            }catch (e : Exception){
                e.printStackTrace()
            }
        }

        camerabtn.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            requestGalleryLauncher.launch(intent)
        }

        savebtn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("프로필 변경")
            dialog.setMessage("선택한 사진으로 프로필을 변경하시겠습니까?")
            dialog.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                savebtn.visibility = View.GONE
                editbtn.visibility = View.VISIBLE
                camerabtn.visibility=View.GONE

                // Volley를 이용한 http 통신
                val updateProfileRequest = object : StringRequest(
                    Request.Method.POST,
                    BuildConfig.API_KEY + "update_worker_profile.php",
                    Response.Listener<String>{ response ->
                        Toast.makeText(this, "사진 등록 성공", Toast.LENGTH_LONG).show()
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                    }){
                    override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                        val params : MutableMap<String, String> = HashMap()
                        params["id"] = MyApplication.prefs.getString("worker_id", "")
                        params["profile"] = encodeImageString.toString()
                        return params
                    }
                }
                val queue = Volley.newRequestQueue(this)
                queue.add(updateProfileRequest)

                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            })
            dialog.setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                savebtn.visibility = View.GONE
                editbtn.visibility = View.VISIBLE
                camerabtn.visibility=View.GONE

                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            })
//            dialog.setNeutralButton("사진 다시 선택", DialogInterface.OnClickListener { dialog, which ->
//                savebtn.visibility = View.VISIBLE
//                editbtn.visibility = View.GONE
//                camerabtn.visibility=View.VISIBLE
//            })
            dialog.show()
        }

        lateinit var name : String
        var status by Delegates.notNull<Int>()

        val today = SimpleDateFormat("yyyy년 M월 d일").format(System.currentTimeMillis())

        binding.workBtn.isEnabled = false
        binding.leaveBtn.isEnabled = false

        // 근무자 정보 불러오기(이름, 사번, 예정 근무 내역, 예정 근무 구역, 예정 근무 시간, 프로필)
        val mainRequest = object : StringRequest(
            Request.Method.POST,
            BuildConfig.API_KEY + "read_information.php",
            Response.Listener<String>{ response ->
                val jsonObject = JSONObject(response)
                val worker = jsonObject.getJSONArray("response").getJSONObject(0)

                name = worker.getString("name")
                MyApplication.prefs.setString("worker_name", name)

                val navName : TextView = headerView.findViewById(R.id.navigationName)
                val navEmplId : TextView = headerView.findViewById(R.id.navigationEmplId)

                navName.text = name
                navEmplId.text = MyApplication.prefs.getString("worker_id", "")

                if(worker.getString("area").equals("")){
                    binding.mainName.text = ""
                    binding.mainArea.text = name + " 님은 " + today
                    binding.mainDate.text = "등록된 작업 일정이 없습니다"
                }
                else{
                    val start_time = SimpleDateFormat("H:mm").format(SimpleDateFormat("H:mm").parse(worker.getString("start")))
                    val end_time = SimpleDateFormat("H:mm").format(SimpleDateFormat("H:mm").parse(worker.getString("end")))
                    binding.mainName.text = name + " 님은 " + today
                    binding.mainArea.text = worker.getString("area") + "구역에서 "
                    binding.mainDate.text = start_time + " ~ " + end_time +"까지 근무입니다"

                    status = worker.getString("status").toInt()
                    MyApplication.prefs.setString("worker_status", worker.getString("status"))

                    if(status == 0){ // 출근 전 상태
                        binding.workBtn.isEnabled = true
                        binding.workBtn.setImageResource(R.drawable.come_btn)
                    }
                    else if(status == 1){ // 출근 후, 퇴근 전 상태
                        binding.leaveBtn.isEnabled = true
                        binding.leaveBtn.setImageResource(R.drawable.come_btn)
                    }
                    else if(status == 2){ // 퇴근 후 상태
                        binding.workBtn.isEnabled = false
                        binding.leaveBtn.isEnabled = false
                        binding.workBtn.setImageResource(R.drawable.out_btn)
                        binding.leaveBtn.setImageResource(R.drawable.out_btn)
                    }
                }

                if(!worker.getString("profile").equals("")){
                    val imageBytes = Base64.decode(worker.getString("profile"), 0)
                    val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    profileImage.setImageBitmap(image)
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


        //날씨
       val weatherRequest= object : StringRequest(
            Method.GET, BuildConfig.WEATHER_API_KEY,
            Response.Listener<String>{ response ->
                val jsonObject = JSONObject(response)
                val city = jsonObject.getString("name")
                //binding.cityView.text=city

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

        val queue2 = Volley.newRequestQueue(this)
        queue2.add(weatherRequest)

        binding.workBtn.setOnClickListener {
            val intent = Intent(this, QrActivity::class.java)
            intent.putExtra("name", name)
            startActivity(intent)
        }

        binding.leaveBtn.setOnClickListener {
            val intent = Intent(this, QrActivity::class.java)
            intent.putExtra("name", name)
            startActivity(intent)
        }

        binding.mainNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        binding.mainSalary.setOnClickListener {
            val intent = Intent(this, SalaryActivity::class.java)
            startActivity(intent)
        }

        binding.mainCalender.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        /*
        binding.mainSos.setOnClickListener{

        }

         */
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) return true

        return super.onOptionsItemSelected(item)
    }

    private fun resize(bm: Bitmap): Bitmap? {
        var bm: Bitmap? = bm
        val config: Configuration = resources.configuration
        if (config.smallestScreenWidthDp >= 800) {
            bm = Bitmap.createScaledBitmap(bm!!, 400, 240, true)
        } else if (config.smallestScreenWidthDp >= 600) {
            bm = Bitmap.createScaledBitmap(bm!!, 300, 180, true)
        } else if(config.smallestScreenWidthDp >= 400){
            bm = Bitmap.createScaledBitmap(bm!!, 200, 120, true)
        } else if(config.smallestScreenWidthDp >= 360){
            bm = Bitmap.createScaledBitmap(bm!!, 180, 108, true)
        } else{
            bm = Bitmap.createScaledBitmap(bm!!, 160, 96, true)
        }
        return bm
    }
}
