package com.example.workers

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityQrBinding
import com.example.workers.databinding.DialogQrBinding
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class QrActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    private var builder: AlertDialog? = null
    lateinit var time : Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityQrBinding.inflate(layoutInflater)
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

        if (MyApplication.prefs.getString("worker_status", "").toInt() == 0) { // 출근 등록을 하려는 경우
            if(MyApplication.prefs.getString("worker_area", "") == ""){
                binding.qrName.text = "출근 등록"
                binding.qrContents.text = "오늘 등록된 작업 일정이 없습니다"
                binding.loginBtn.isEnabled = false
            }
            else{
                binding.qrName.text = "출근 등록"

                binding.loginBtn.setOnClickListener {
                    val integrator = IntentIntegrator(this)
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // 여러가지 바코드중에 특정 바코드 설정 가능
                    integrator.setPrompt("QR 코드를 스캔 해주세요") // 스캔할 때 하단의 문구
                    integrator.setCameraId(0) // 0은 후면 카메라, 1은 전면 카메라
                    integrator.setBeepEnabled(true) // 바코드를 인식했을 때 삑 소리유무
                    integrator.setBarcodeImageEnabled(false) // 스캔 했을 때 스캔한 이미지 사용여부
                    integrator.initiateScan() // 스캔
                }
            }

            binding.goToMainBtn.setOnClickListener {
                finish()
            }
        } else if (MyApplication.prefs.getString("worker_status", "")
                .toInt() == 1
        ) { // 퇴근 등록을 하려는 경우
            binding.qrName.text = "퇴근 등록"

            binding.loginBtn.setOnClickListener {
                val integrator = IntentIntegrator(this)
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // 여러가지 바코드중에 특정 바코드 설정 가능
                integrator.setPrompt("QR 코드를 스캔 해주세요") // 스캔할 때 하단의 문구
                integrator.setCameraId(0) // 0은 후면 카메라, 1은 전면 카메라
                integrator.setBeepEnabled(true) // 바코드를 인식했을 때 삑 소리유무
                integrator.setBarcodeImageEnabled(false) // 스캔 했을 때 스캔한 이미지 사용여부
                integrator.initiateScan() // 스캔
            }
            binding.goToMainBtn.setOnClickListener {
                finish()
            }
        } else if (MyApplication.prefs.getString("worker_status", "").toInt() == 2) { // 퇴근이 끝난 경우
            binding.qrName.text = "퇴근 완료"
            binding.qrContents.text = "오늘 퇴근이 완료되었습니다"
            binding.loginBtn.isEnabled = false

            binding.goToMainBtn.setOnClickListener {
                finish()
            }

        }


    }

    override fun onDestroy() {
        super.onDestroy()

        if (builder != null && builder!!.isShowing) { //다이얼로그가 띄워져 있는 상태(showing)인 경우 dismiss() 호출
            builder!!.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) { // 뒤로가기 선택 시
                Toast.makeText(this, "QR코드 인증이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            } else { // QR코드가 스캔된 경우
                if (MyApplication.prefs.getString("worker_status", "").toInt() == 0) { // 출근 등록 시
                    val dialog = Dialog(this)
                    dialog.setContentView(R.layout.dialog_qr)
                    dialog.setCanceledOnTouchOutside(true)
                    dialog.setCancelable(false)
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val dialogName = dialog.findViewById<TextView>(R.id.dialogName)
                    val dialogEmplId = dialog.findViewById<TextView>(R.id.dialogEmplId)
                    val dialogContents = dialog.findViewById<TextView>(R.id.dialogContents)
                    val dialogOkBtn = dialog.findViewById<Button>(R.id.dialogOkBtn)
                    val dialogProfile = dialog.findViewById<ImageView>(R.id.dialogProfile)

                    dialogName.text = MyApplication.prefs.getString("worker_name", "")
                    dialogEmplId.text =
                        MyApplication.prefs.getString("worker_id", "")
                    time = Calendar.getInstance().time

                    dialogContents.text =
                        SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(time) + "\n" + SimpleDateFormat(
                            "a HH:mm"
                            , Locale.KOREA).format(time) + " 출근을 등록합니다"
                    if(MyApplication.prefs.getString("worker_profile", "").equals("")){
                        dialogProfile.setImageResource(R.drawable.profile_default)
                    }
                    else{
                        val imageBytes = Base64.decode(MyApplication.prefs.getString("worker_profile", ""), 0)
                        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        dialogProfile.setImageBitmap(image)
                    }
                    dialogOkBtn.setOnClickListener {
                        val workRequest = object : StringRequest(
                            Request.Method.POST,
                            "http://ec2-15-165-242-180.ap-northeast-2.compute.amazonaws.com/go_work.php",
                            Response.Listener<String> { response ->
                                if (response.toString().equals("-1")) { // QR 인증 실패
                                    Toast.makeText(this, "등록된 안전모가 아닙니다", Toast.LENGTH_LONG).show()
                                } else if (response.toString().equals("0")) { // 안전모 등록 실패
                                    Toast.makeText(this, "이미 사용중인 안전모입니다", Toast.LENGTH_LONG)
                                        .show()
                                } else { // QR 인증 성공 + 안전모 등록 성공
                                    Toast.makeText(this, "오늘 출근이 등록되었습니다", Toast.LENGTH_LONG)
                                        .show()

                                    finish()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            },
                            Response.ErrorListener { error ->
                                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                            }) {
                            override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                                val params: MutableMap<String, String> = HashMap()
                                params["worker_pkey"] =
                                    MyApplication.prefs.getString("worker_pkey", "")
                                params["hat_number"] = result.contents
                                params["time"] =  SimpleDateFormat("HH:mm").format(time)
                                return params
                            }
                        }
                        val queue = Volley.newRequestQueue(this)
                        queue.add(workRequest)
                    }
                    dialog.show()

                } else if (MyApplication.prefs.getString("worker_status", "").toInt() == 1) { // 퇴근 등록 시
                    val dialog = Dialog(this)
                    dialog.setContentView(R.layout.dialog_qr)
                    dialog.setCanceledOnTouchOutside(true)
                    dialog.setCancelable(false)
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val dialogName = dialog.findViewById<TextView>(R.id.dialogName)
                    val dialogEmplId = dialog.findViewById<TextView>(R.id.dialogEmplId)
                    val dialogContents = dialog.findViewById<TextView>(R.id.dialogContents)
                    val dialogOkBtn = dialog.findViewById<Button>(R.id.dialogOkBtn)
                    val dialogProfile = dialog.findViewById<ImageView>(R.id.dialogProfile)

                    dialogName.text = MyApplication.prefs.getString("worker_name", "")
                    dialogEmplId.text =
                        MyApplication.prefs.getString("worker_id", "")
                    time = Calendar.getInstance().time
                    dialogContents.text = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(time) + "\n" + SimpleDateFormat(
                        "a HH:mm"
                        , Locale.KOREA).format(time) + " 퇴근을 등록합니다"
                    if(MyApplication.prefs.getString("worker_profile", "").equals("")){
                        dialogProfile.setImageResource(R.drawable.profile_default)
                    }
                    else{
                        val imageBytes = Base64.decode(MyApplication.prefs.getString("worker_profile", ""), 0)
                        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        dialogProfile.setImageBitmap(image)
                    }
                    dialogOkBtn.setOnClickListener {
                        val leaveRequest = object : StringRequest(
                            Request.Method.POST,
                            "http://ec2-15-165-242-180.ap-northeast-2.compute.amazonaws.com/leave_work.php",
                            Response.Listener<String> { response ->
                                if (response.toString().equals("-1")) { // QR 인증 실패
                                    Toast.makeText(this, "현재 사용중인 안전모가 아닙니다.", Toast.LENGTH_LONG)
                                        .show()
                                } else { // QR 인증 성공 + 안전모 등록 성공
                                    Toast.makeText(this, "오늘 퇴근이 등록되었습니다.", Toast.LENGTH_LONG)
                                        .show()
                                    finish()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            },
                            Response.ErrorListener { error ->
                                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                            }) {
                            override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                                val params: MutableMap<String, String> = HashMap()
                                params["worker_pkey"] =
                                    MyApplication.prefs.getString("worker_pkey", "")
                                params["hat_number"] = result.contents
                                params["time"] =  SimpleDateFormat("HH:mm").format(time)
                                return params
                            }
                        }
                        val queue = Volley.newRequestQueue(this)
                        queue.add(leaveRequest)
                    }
                    dialog.show()
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
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
