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
import com.google.zxing.integration.android.IntentIntegrator

class QrActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.qrBtn.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // 여러가지 바코드중에 특정 바코드 설정 가능
            integrator.setPrompt("QR 코드를 스캔하여 주세요:)") // 스캔할 때 하단의 문구
            integrator.setCameraId(0) // 0은 후면 카메라, 1은 전면 카메라
            integrator.setBeepEnabled(true) // 바코드를 인식했을 때 삑 소리유무
            integrator.setBarcodeImageEnabled(false) // 스캔 했을 때 스캔한 이미지 사용여부
            integrator.initiateScan() // 스캔
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) { // 뒤로가기 선택 시
                Toast.makeText(this, "QR코드 인증이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            }
            else { // QR코드가 스캔된 경우
                val qrRequest = object : StringRequest(
                    Request.Method.POST,
                    "http://IP주소/qr.php",
                    Response.Listener<String>{ response ->
                        if(response.toInt() == -1){ // QR 인증 실패
                            Toast.makeText(this, "등록된 안전모가 아닙니다.", Toast.LENGTH_LONG).show()
                        }
                        else if(response.toInt() == 0){ // 안전모 등록 실패
                            Toast.makeText(this, "이미 사용중인 안전모입니다.", Toast.LENGTH_LONG).show()
                        }
                        else{ // QR 인증 성공 + 안전모 등록 성공
                            Toast.makeText(this, "안전모 등록이 완료되었습니다.", Toast.LENGTH_LONG).show()
                        }
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                    }){
                    override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                        val params : MutableMap<String, String> = HashMap()
                        params["worker_pkey"] = MyApplication.prefs.getString("worker_pkey", "")
                        params["hat_number"] = result.contents
                        return params
                    }
                }
                val queue = Volley.newRequestQueue(this)
                queue.add(qrRequest)
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}