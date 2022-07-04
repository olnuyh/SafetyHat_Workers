package com.example.workers

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupBtn.setOnClickListener {
            val signupId = binding.signupId.text.toString() // 사용자가 입력한 ID
            val signupPw = binding.signupPw.text.toString() // 사용자가 입력한 PW

            // Volley를 이용한 http 통신
            val signupRequest = object : StringRequest(
                Request.Method.POST,
                "http://ip주소/signup.php",
                Response.Listener<String>{ response ->
                    if(response.toInt() == -1){ // 회원가입 실패
                        Toast.makeText(this, "이미 존재하는 아이디입니다.", Toast.LENGTH_LONG).show()
                        binding.signupId.text = null
                        binding.signupPw.text = null
                    }
                    else if(response.toInt() == 1){ // 회원가입 성공
                        Toast.makeText(this, "회원 가입되었습니다.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                }){
                override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                    val params : MutableMap<String, String> = HashMap()
                    params["id"] = signupId
                    params["pw"] = signupPw
                    return params
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(signupRequest)
        }
    }
}