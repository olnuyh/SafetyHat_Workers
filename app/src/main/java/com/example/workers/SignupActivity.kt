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
            val signupName = binding.signupName.text.toString() // 사용자가 입력한 이름
            val signupId = binding.signupId.text.toString() // 사용자가 입력한 ID(사원번호)
            val signupPw = binding.signupPw.text.toString() // 사용자가 입력한 PW
            val signupPwCheck = binding.signupPwCheck.text.toString() // 사용자가 입력한 PW 확인

            if(signupName.equals("")){ // 이름을 입력하지 않은 경우
                Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else if(signupId.equals("")){ // 아이디(사원번호)를 입력하지 않은 경우
                Toast.makeText(this, "아이디(사원번호)를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else if(signupPw.equals("")){ // 비밀번호를 입력하지 않은 경우
                Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else if(signupPwCheck.equals("")){ // 비밀번호 확인을 입력하지 않은 경우
                Toast.makeText(this, "비밀번호를 한번 더 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else{ // 모든 정보 입력 시
                // Volley를 이용한 http 통신
                val signupRequest = object : StringRequest(
                    Request.Method.POST,
                    BuildConfig.API_KEY + "signup.php",
                    Response.Listener<String>{ response ->
                        if(response.toString().equals("-1")){ // 회원가입 실패
                            Toast.makeText(this, "이미 존재하는 아이디입니다.", Toast.LENGTH_LONG).show()
                            binding.signupId.text = null
                            binding.signupPw.text = null
                        }
                        else if(response.equals("1")){ // 회원가입 성공
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
                        params["name"] = signupName
                        return params
                    }
                }

                val queue = Volley.newRequestQueue(this)
                queue.add(signupRequest)
            }
        }
    }
}