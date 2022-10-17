package com.example.workers

import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.workers.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates



class MainActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var binding : ActivityMainBinding
    lateinit var speechRecognizer : SpeechRecognizer
    val database = Firebase.database("https://safetyhat-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val ref = database.getReference("SosMessages")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //getFCMToken()

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }

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

        var encodeImageString: String? = null

        val headerView = binding.mainDrawerView.getHeaderView(0)
        val editbtn=headerView.findViewById<ImageButton>(R.id.navigationEditBtn)
        val camerabtn=headerView.findViewById<ImageButton>(R.id.navigationCameraBtn)
        val savebtn=headerView.findViewById<ImageButton>(R.id.navigationSaveBtn)
        val profileImage=headerView.findViewById<ImageView>(R.id.navigationProfile)
        val xbtn=headerView.findViewById<ImageButton>(R.id.navigationCancel)
        val edittext=headerView.findViewById<TextView>(R.id.edittext)
        val savetext=headerView.findViewById<TextView>(R.id.savetext)

        xbtn.setOnClickListener {
            binding.drawerLayout.closeDrawers()

            camerabtn.visibility=View.INVISIBLE
            editbtn.visibility=View.VISIBLE
            savebtn.visibility=View.INVISIBLE
            savetext.visibility=View.INVISIBLE
            edittext.visibility=View.VISIBLE

            if(MyApplication.prefs.getString("worker_profile", "").equals("")){
                profileImage.setImageResource(R.drawable.profile_default)
            }
            else{
                val imageBytes = Base64.decode(MyApplication.prefs.getString("worker_profile", ""), 0)
                val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImage.setImageBitmap(image)
            }
        }

        editbtn.setOnClickListener {
            camerabtn.visibility=View.VISIBLE
            editbtn.visibility=View.INVISIBLE
            savebtn.visibility=View.VISIBLE
            savetext.visibility=View.VISIBLE
            edittext.visibility=View.INVISIBLE
        }

        savebtn.setOnClickListener{
            camerabtn.visibility=View.GONE
            editbtn.visibility=View.VISIBLE
            savebtn.visibility=View.GONE
            savetext.visibility=View.GONE
            edittext.visibility=View.VISIBLE
        }


        // 갤러리 연동
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){

            try{
                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                Log.d("mobileApp", bitmap.toString())
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
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_profile)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val cancelButton = dialog.findViewById<ImageButton>(R.id.cancelButton)
            val okButton = dialog.findViewById<ImageButton>(R.id.okButton)

            okButton.setOnClickListener {
                savebtn.visibility = View.GONE
                savetext.visibility = View.GONE
                editbtn.visibility = View.VISIBLE
                edittext.visibility = View.VISIBLE
                camerabtn.visibility=View.GONE

                // Volley를 이용한 http 통신
                val updateProfileRequest = object : StringRequest(
                    Request.Method.POST,
                    BuildConfig.API_KEY + "update_worker_profile.php",
                    Response.Listener<String>{ response ->
                        Toast.makeText(this, "사진 등록 성공", Toast.LENGTH_LONG).show()
                        //MyApplication.prefs.setString("worker_profile", ))
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
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()

                savebtn.visibility = View.VISIBLE
                savetext.visibility = View.VISIBLE
                editbtn.visibility = View.GONE
                edittext.visibility = View.GONE
                camerabtn.visibility=View.VISIBLE
            }

            dialog.show()
        }

        lateinit var name : String
        var status by Delegates.notNull<Int>()

        val today = SimpleDateFormat("yyyy년 M월 d일").format(System.currentTimeMillis())

        binding.workBtn.isEnabled = false
        binding.leaveBtn.isEnabled = false

        // 근무자 정보 불러오기(이름, 사번, 예정 근무 내역, 예정 근무 구역, 예정 근무 시간, 프로필)
        val mainRequest = @SuppressLint("ResourceAsColor")
        object : StringRequest(
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
                    binding.mainName.text = name + " 님은 "
                    binding.mainArea.text = today
                    binding.mainDate.text = "등록된 작업 일정이 없습니다"

                }
                else{
                    val start_time = SimpleDateFormat("H:mm").format(SimpleDateFormat("H:mm").parse(worker.getString("start")))
                    val end_time = SimpleDateFormat("H:mm").format(SimpleDateFormat("H:mm").parse(worker.getString("end")))
                    binding.mainName.text = name + " 님은 " + today
                    binding.mainArea.text = worker.getString("area") + "구역에서 "
                    binding.mainDate.text = start_time + " ~ " + end_time +"까지 근무입니다"

                    var textView:TextView = findViewById(R.id.mainArea)
                    val textData: String = textView.text.toString()
                    val builder = SpannableStringBuilder(textData)
                    val colorBlueSpan = ForegroundColorSpan(Color.rgb(62,79,135))
                    builder.setSpan(colorBlueSpan, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    textView.text = builder

                    var textView2:TextView = findViewById(R.id.mainDate)
                    val textData2: String = textView2.text.toString()
                    val builder2 = SpannableStringBuilder(textData2)
                    builder2.setSpan(colorBlueSpan, 0, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    textView2.text = builder2

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

                MyApplication.prefs.setString("worker_profile", worker.getString("profile"))

                if(!MyApplication.prefs.getString("worker_profile", "").equals("")){
                    val imageBytes = Base64.decode(MyApplication.prefs.getString("worker_profile", ""), 0)
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

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)    // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

        // 마이크 버튼 눌러서 음성인식 시작
        binding.sosMicBtn.setOnClickListener {
            binding.sosSendMessage.text = ""
            // 새 SpeechRecognizer 생성
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(recognitionListener)    // 리스너 설정
            speechRecognizer.startListening(intent)                         // 듣기 시작
        }

        binding.sosSendBtn.setOnClickListener {
            if(binding.sosSendMessage.text.equals("")){
                Toast.makeText(this, "음성 메시지를 입력하세요", Toast.LENGTH_SHORT).show()
            }else{
                val message = SosMessage(MyApplication.prefs.getString("worker_pkey", ""),
                    MyApplication.prefs.getString("worker_name", ""),
                    binding.sosSendMessage.text.toString(),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
                ref.push().setValue(message)

                val content = MyApplication.prefs.getString("worker_name", "") + ": " + binding.sosSendMessage.text.toString()

                binding.sosSendMessage.text = ""
                binding.sosText.text = "버튼을 누르고 음성인식을 시작하세요"
                binding.sosSendMessage.isEnabled = false


                // Volley를 이용한 http 통신
                val sosRequest = object : StringRequest(
                    Request.Method.POST,
                    BuildConfig.API_KEY + "send_sosnotification.php",
                    Response.Listener<String>{ response ->

                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                    }){
                    override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                        val params : MutableMap<String, String> = HashMap()
                        params["content"] = content
                        return params
                    }
                }
                val queue = Volley.newRequestQueue(this)
                queue.add(sosRequest)

                Toast.makeText(this, "전송되었습니다", Toast.LENGTH_SHORT).show()

            }
        }

        binding.mainSos.setOnClickListener {
            val intent = Intent(this, SosActivity::class.java)
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


        binding.mainSos.setOnClickListener{
            val intent = Intent(this, SosActivity::class.java)
            startActivity(intent)
        }

    }

    // 리스너 설정
    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        // 음성 녹음할 준비가 되면 호출
        override fun onReadyForSpeech(params: Bundle) {
        }
        // 말하기 시작했을 때 호출
        override fun onBeginningOfSpeech() {
            binding.sosText.text = "잘 듣고 있어요"
        }
        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}
        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}
        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            binding.sosText.text = "아래 내용으로 녹음되었어요. 전송 버튼을 누르세요"
        }
        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러가 발생했습니다"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러가 발생했습니다"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한이 부족합니다"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러가 발생했습니다"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 작업 시간이 초과되었습니다"
                SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 인식 결과가 없습니다"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService가 사용 중입니다"
                SpeechRecognizer.ERROR_SERVER -> "서버가 오류 상태를 보냅니다"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성인식 시간을 초과했습니다"
                else -> "알 수 없는 에러가 발생했습니다"
            }
            binding.sosText.text = "$message"
        }
        // 인식 결과가 준비되면 호출
        override fun onResults(results: Bundle) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            // 결과 출력
            for (i in matches!!.indices) binding.sosSendMessage.text = matches[i]
        }
        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}
        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) return true

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            val headerView = binding.mainDrawerView.getHeaderView(0)
            val editbtn=headerView.findViewById<ImageButton>(R.id.navigationEditBtn)
            val camerabtn=headerView.findViewById<ImageButton>(R.id.navigationCameraBtn)
            val savebtn=headerView.findViewById<ImageButton>(R.id.navigationSaveBtn)
            val edittext=headerView.findViewById<TextView>(R.id.edittext)
            val savetext=headerView.findViewById<TextView>(R.id.savetext)
            val profileImage=headerView.findViewById<ImageView>(R.id.navigationProfile)

            camerabtn.visibility=View.INVISIBLE
            editbtn.visibility=View.VISIBLE
            savebtn.visibility=View.INVISIBLE
            savetext.visibility=View.INVISIBLE
            edittext.visibility=View.VISIBLE

            if(MyApplication.prefs.getString("worker_profile", "").equals("")){
                profileImage.setImageResource(R.drawable.profile_default)
            }
            else{
                val imageBytes = Base64.decode(MyApplication.prefs.getString("worker_profile", ""), 0)
                val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImage.setImageBitmap(image)
            }
        } else {
            super.onBackPressed()
        }
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

    private fun getFCMToken(): String?{
        var token: String? = null
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                //Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            token = task.result

            // Log and toast
            Log.d("mobileApp", "FCM Token is ${token}")
        })

        return token
    }
}
