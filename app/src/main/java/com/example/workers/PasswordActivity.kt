package com.example.workers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityPasswordBinding

class PasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.findPasswordBtn.setOnClickListener {
            val name = binding.passwordName.text.toString() // 사용자가 입력한 이름
            val id = binding.passwordId.text.toString() // 사용자가 입력한 ID(사원번호)

            if(name.equals("")){ // 이름을 입력하지 않은 경우
                Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else if(id.equals("")){ // 아이디(사원번호)를 입력하지 않은 경우
                Toast.makeText(this, "아이디(사원번호)를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else{ // 모든 정보 입력 시
                // Volley를 이용한 http 통신
                val findPasswordRequest = object : StringRequest(
                    Request.Method.POST,
                    BuildConfig.API_KEY + "find_password.php",
                    Response.Listener<String>{ response ->
                        if(response.toString().equals("-1")){ // 비밀번호 찾기 실패
                            Toast.makeText(this, "존재하지 않는 회원입니다.", Toast.LENGTH_LONG).show()
                            binding.passwordName.text = null
                            binding.passwordId.text = null
                        }
                        else{ // 비밀번호 찾기 성공
                            binding.password.text = "회원님의 비밀번호는 \" " + response + " \" 입니다"
                        }
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                    }){
                    override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                        val params : MutableMap<String, String> = HashMap()
                        params["name"] = name
                        params["id"] = id
                        return params
                    }
                }

                val queue = Volley.newRequestQueue(this)
                queue.add(findPasswordRequest)
            }
        }

        binding.goToLoginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}