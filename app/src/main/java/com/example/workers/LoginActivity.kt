package com.example.workers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.DefaultRetryPolicy
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

        val auto_loginId = MyApplication.prefs.getString("worker_id", "")
        val auto_loginPw = MyApplication.prefs.getString("worker_pw", "")

       // if(!auto_loginId.equals("") && !auto_loginPw.equals("")){ // 자동 로그인
            //Toast.makeText(this, "자동 로그인 성공하였습니다.", Toast.LENGTH_LONG).show()
            //finish()

           // val intent = Intent(this, MainActivity::class.java)
            //startActivity(intent)
       //}
       // else if(auto_loginId.equals("") && auto_loginPw.equals("")){

            binding.loginbtn.setOnClickListener {
                val loginId = binding.loginId.text.toString() // 사용자가 입력한 ID(사원번호)
                val loginPw = binding.loginPw.text.toString() // 사용자가 입력한 비밀번호

                if(loginId.equals("")){ // 아이디를 입력하지 않은 경우
                    Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show()
                }
                else if(loginPw.equals("")){ // 비밀번호를 입력하지 않은 경우
                    Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                }
                else{
                    // Volley를 이용한 http 통신
                    val loginRequest = object : StringRequest(
                        Request.Method.POST,
                        BuildConfig.API_KEY + "workers_login.php",
                        Response.Listener<String>{ response ->
                            if(response.toString().equals("-1")){ // 로그인 실패
                                Toast.makeText(this, "아이디와 비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show()
                                binding.loginId.text = null
                                binding.loginPw.text = null
                            }
                            else{ // 로그인 성공
                                MyApplication.prefs.setString("worker_id", binding.loginId.text.toString())
                                MyApplication.prefs.setString("worker_pw", binding.loginPw.text.toString())
                                MyApplication.prefs.setString("worker_pkey", response.toString())
                                Toast.makeText(this, "로그인 성공하였습니다.", Toast.LENGTH_LONG).show()
                                finish()
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
            }
        //}

        binding.findBtn.setOnClickListener {
            val intent = Intent(this, PasswordActivity::class.java)
            startActivity(intent)
        }

        binding.goToSignupBtn.setOnClickListener { // 회원가입 페이지로 이동
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}