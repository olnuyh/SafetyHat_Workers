package com.example.workers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.workers.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.text = MyApplication.prefs.getString("worker_id", "")
        binding.textView2.text = MyApplication.prefs.getString("worker_pw", "")
        binding.textView3.text = MyApplication.prefs.getString("worker_pkey", "")
        binding.goToQrBtn.setOnClickListener { // QR 인증 페이지로 이동
            val intent = Intent(this, QrActivity::class.java)
            startActivity(intent)
        }
    }
}