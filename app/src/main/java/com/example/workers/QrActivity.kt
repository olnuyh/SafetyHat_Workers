package com.example.workers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityQrBinding
import com.example.workers.databinding.DialogQrBinding
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import kotlin.properties.Delegates

class QrActivity : AppCompatActivity() {
    var status by Delegates.notNull<Int>()
    private var builder: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        status = intent.getIntExtra("status", 0)

        if (status == 0) { // 출근 등록을 하려는 경우
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
        } else { // 퇴근 등록을 하려는 경우
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
        }

        binding.goToMainBtn.setOnClickListener {
            finish()
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
                if (status == 0) { // 출근 등록 시
                    val workRequest = object : StringRequest(
                        Request.Method.POST,
                        BuildConfig.API_KEY + "go_work.php",
                        Response.Listener<String> { response ->
                            if (response.toString().equals("-1")) { // QR 인증 실패
                                Toast.makeText(this, "등록된 안전모가 아닙니다.", Toast.LENGTH_LONG).show()
                            } else if (response.toString().equals("0")) { // 안전모 등록 실패
                                Toast.makeText(this, "이미 사용중인 안전모입니다.", Toast.LENGTH_LONG).show()
                            } else { // QR 인증 성공 + 안전모 등록 성공
                                val dialog = DialogQrBinding.inflate(layoutInflater)
                                dialog.dialogName.text = intent.getStringExtra("name")
                                dialog.dialogEmplId.text = MyApplication.prefs.getString("worker_id", "")
                                val input = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(response.toString())
                                dialog.dialogContents.text = SimpleDateFormat("yyyy년 M월 d일").format(input) + "\n" + SimpleDateFormat("a HH:mm").format(input) + " 출근을 등록합니다"
                                dialog.dialogOkBtn.setOnClickListener {
                                    finish()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                builder = AlertDialog.Builder(this)
                                    .setView(dialog.root)
                                    .show()
                            }
                        },
                        Response.ErrorListener { error ->
                            Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                        }) {
                        override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                            val params: MutableMap<String, String> = HashMap()
                            params["worker_pkey"] = MyApplication.prefs.getString("worker_pkey", "")
                            params["hat_number"] = result.contents
                            return params
                        }
                    }
                    val queue = Volley.newRequestQueue(this)
                    queue.add(workRequest)
                }
                else if(status == 1){ // 퇴근 등록 시
                    val leaveRequest = object : StringRequest(
                        Request.Method.POST,
                        BuildConfig.API_KEY + "leave_work.php",
                        Response.Listener<String> { response ->
                            if (response.toString().equals("-1")) { // QR 인증 실패
                                Toast.makeText(this, "현재 사용중인 안전모가 아닙니다.", Toast.LENGTH_LONG).show()
                            } else { // QR 인증 성공 + 안전모 등록 성공
                                val dialog = DialogQrBinding.inflate(layoutInflater)
                                dialog.dialogName.text = intent.getStringExtra("name")
                                dialog.dialogEmplId.text = MyApplication.prefs.getString("worker_id", "")
                                val input = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(response.toString())
                                dialog.dialogContents.text = SimpleDateFormat("yyyy년 M월 d일").format(input) + "\n" + SimpleDateFormat("a HH:mm").format(input) + " 퇴근을 등록합니다"
                                dialog.dialogOkBtn.setOnClickListener {
                                    finish()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                builder = AlertDialog.Builder(this)
                                    .setView(dialog.root)
                                    .show()
                            }
                        },
                        Response.ErrorListener { error ->
                            Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                        }) {
                        override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                            val params: MutableMap<String, String> = HashMap()
                            params["worker_pkey"] = MyApplication.prefs.getString("worker_pkey", "")
                            params["hat_number"] = result.contents
                            return params
                        }
                    }
                    val queue = Volley.newRequestQueue(this)
                    queue.add(leaveRequest)
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
