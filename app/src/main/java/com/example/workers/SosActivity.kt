package com.example.workers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workers.databinding.ActivitySosBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class SosActivity : AppCompatActivity() {
    lateinit var binding : ActivitySosBinding
    lateinit var speechRecognizer : SpeechRecognizer
    val database = Firebase.database("https://safetyhat-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val ref = database.getReference("SosMessages")
    val messageList = ArrayList<SosMessage>()
    lateinit var adapter : SosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sosRecyclerView.layoutManager = LinearLayoutManager(this)

        /*
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }

         */

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)    // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

        // 마이크 버튼 눌러서 음성인식 시작
        binding.micBtn.setOnClickListener {
            binding.recordedContents.text = ""
            // 새 SpeechRecognizer 생성
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(recognitionListener)    // 리스너 설정
            speechRecognizer.startListening(intent)                         // 듣기 시작
        }

        binding.saveContentsBtn.setOnClickListener {
            val message = SosMessage(MyApplication.prefs.getString("worker_pkey", ""),
                MyApplication.prefs.getString("worker_name", ""),
                binding.recordedContents.text.toString(),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
            ref.push().setValue(message)

            binding.recordedContents.text = ""
            binding.explanationContents.text = "버튼을 누르고 음성인식을 시작하세요"
            binding.saveContentsBtn.isEnabled = false
        }

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()

                for(msg in snapshot.children){
                    val message = msg.getValue(SosMessage::class.java)
                    if(message?.pkey.equals(MyApplication.prefs.getString("worker_pkey", ""))){
                        messageList.add(message!!)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        adapter = SosAdapter(this, messageList)
        binding.sosRecyclerView.adapter = adapter
    }

    // 리스너 설정
    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        // 음성 녹음할 준비가 되면 호출
        override fun onReadyForSpeech(params: Bundle) {
        }
        // 말하기 시작했을 때 호출
        override fun onBeginningOfSpeech() {
            binding.explanationContents.text = "잘 듣고 있어요."
        }
        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}
        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}
        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            binding.saveContentsBtn.isEnabled = true
            binding.explanationContents.text = "아래 내용으로 녹음되었어요. 전송 버튼을 누르세요."
        }
        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러가 발생했습니다"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러가 발생했습니다"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한이 부족합니다"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러가 발생했습니다"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 작업 시간이 초과되었습니다"
                SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 인식 결과가 없습니다"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService가 사용 중입니다"
                SpeechRecognizer.ERROR_SERVER -> "서버가 오류 상태를 보냅니다"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성인식 시간을 초과했습니다"
                else -> "알 수 없는 에러가 발생했습니다"
            }
            binding.explanationContents.text = "$message"
        }
        // 인식 결과가 준비되면 호출
        override fun onResults(results: Bundle) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            // 결과 출력
            for (i in matches!!.indices) binding.recordedContents.text = matches[i]
        }
        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}
        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }
}