package com.example.workers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.workers.databinding.ActivityQrBinding
import com.google.zxing.integration.android.IntentIntegrator

class QrActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.qrStartBtn.setOnClickListener {
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

        // QR 코드를 찍은 결과를 변수에 담는다.
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        Log.d("TTT", "QR 코드 체크")

        //결과가 있으면
        if (result != null) {
            // 컨텐츠가 없으면
            if (result.contents == null) {
                //토스트를 띄운다.
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
            // 컨텐츠가 있으면
            else {
                //토스트를 띄운다.
                Toast.makeText(this, "scanned" + result.contents, Toast.LENGTH_LONG).show()
                Log.d("TTT", "QR 코드 URL:${result.contents}")

                // Dialog만들기
                val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)
                    .setTitle("안전모 QR 등록")

                mBuilder.show()

                val okButton = mDialogView.findViewById<Button>(R.id.dialog_success_btn)
                okButton.setOnClickListener {
                    startActivity(Intent(this,QrActivity::class.java))
                }

            }
            // 결과가 없으면
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}