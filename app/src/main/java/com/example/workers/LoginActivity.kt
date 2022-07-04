package com.example.workers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val loginId = binding.loginId.text.toString()
            val loginPw = binding.loginPw.text.toString()

            // Volley를 이용한 http 통신
            val loginRequest = object : StringRequest(
                Request.Method.POST,
                "http://ip주소/login.php",
                Response.Listener<String>{ response ->
                    if(response.toInt() == -1){ // 로그인 실패
                        Toast.makeText(this, "아이디와 비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show()
                        binding.loginId.text = null
                        binding.loginPw.text = null
                    }
                    else if(response.toInt() == 1){ // 로그인 성공
                        Toast.makeText(this, "로그인 성공하였습니다.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                }){
                override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                    val params : MutableMap<String, String> = HashMap()
                    params["id"] = loginId
                    params["pw"] = loginPw
                    return params
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(loginRequest)
        }

        binding.goToSignupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}