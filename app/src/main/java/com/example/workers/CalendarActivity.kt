package com.example.workers

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivityCalendarBinding
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class CalendarActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle

    private lateinit var binding :ActivityCalendarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toggle.syncState()

        binding.calendarDrawerView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.menuQr -> {
                    val intent = Intent(this, QrActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuNotification -> {
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuCalendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuSos -> {
                    val intent = Intent(this, SosActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuSalary -> {
                    val intent = Intent(this, SalaryActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        binding.logout.setOnClickListener {
            MyApplication.prefs.clear()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        var encodeImageString: String? = null

        val headerView = binding.calendarDrawerView.getHeaderView(0)
        val editbtn=headerView.findViewById<ImageButton>(R.id.navigationEditBtn)
        val camerabtn=headerView.findViewById<ImageButton>(R.id.navigationCameraBtn)
        val savebtn=headerView.findViewById<ImageButton>(R.id.navigationSaveBtn)
        val profileImage=headerView.findViewById<ImageView>(R.id.navigationProfile)
        val xbtn=headerView.findViewById<ImageButton>(R.id.navigationCancel)
        val edittext=headerView.findViewById<TextView>(R.id.edittext)
        val savetext=headerView.findViewById<TextView>(R.id.savetext)

        xbtn.setOnClickListener {
            binding.drawerLayout.closeDrawers()

            camerabtn.visibility=View.INVISIBLE
            editbtn.visibility=View.VISIBLE
            savebtn.visibility=View.INVISIBLE
            savetext.visibility=View.INVISIBLE
            edittext.visibility=View.VISIBLE

            if(MyApplication.prefs.getString("worker_profile", "").equals("")){
                profileImage.setImageResource(R.drawable.profile_default)
            }
            else{
                val imageBytes = Base64.decode(MyApplication.prefs.getString("worker_profile", ""), 0)
                val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImage.setImageBitmap(image)
            }
        }

        editbtn.setOnClickListener {
            camerabtn.visibility=View.VISIBLE
            editbtn.visibility=View.INVISIBLE
            savebtn.visibility=View.VISIBLE
            savetext.visibility=View.VISIBLE
            edittext.visibility=View.INVISIBLE
        }

        savebtn.setOnClickListener{
            camerabtn.visibility=View.GONE
            editbtn.visibility=View.VISIBLE
            savebtn.visibility=View.GONE
            savetext.visibility=View.GONE
            edittext.visibility=View.VISIBLE
        }

        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){

            try{
                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                Log.d("mobileApp", bitmap.toString())
                inputStream!!.close()
                inputStream = null

                val resizedBitmap = resize(bitmap)
                profileImage.setImageBitmap(resizedBitmap)

                //DB에 저장할 형태로 변경
                val byteArrayOutputStream = ByteArrayOutputStream()
                resizedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val bytesOfImage: ByteArray = byteArrayOutputStream.toByteArray()
                encodeImageString = Base64.encodeToString(bytesOfImage, Base64.DEFAULT)

            }catch (e : Exception){
                e.printStackTrace()
            }
        }

        camerabtn.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            requestGalleryLauncher.launch(intent)
        }

        savebtn.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_profile)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val cancelButton = dialog.findViewById<ImageButton>(R.id.cancelButton)
            val okButton = dialog.findViewById<ImageButton>(R.id.okButton)

            okButton.setOnClickListener {
                savebtn.visibility = View.GONE
                savetext.visibility = View.GONE
                editbtn.visibility = View.VISIBLE
                edittext.visibility = View.VISIBLE
                camerabtn.visibility=View.GONE

                // Volley를 이용한 http 통신
                val updateProfileRequest = object : StringRequest(
                    Request.Method.POST,
                    "http://ec2-15-165-242-180.ap-northeast-2.compute.amazonaws.com/update_worker_profile.php",
                    Response.Listener<String>{ response ->
                        Toast.makeText(this, "사진 등록 성공", Toast.LENGTH_LONG).show()
                        MyApplication.prefs.setString("worker_profile", encodeImageString.toString())
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                    }){
                    override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                        val params : MutableMap<String, String> = java.util.HashMap()
                        params["id"] = MyApplication.prefs.getString("worker_id", "")
                        params["profile"] = encodeImageString.toString()
                        return params
                    }
                }
                val queue = Volley.newRequestQueue(this)
                queue.add(updateProfileRequest)

                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()

                savebtn.visibility = View.VISIBLE
                savetext.visibility = View.VISIBLE
                editbtn.visibility = View.GONE
                edittext.visibility = View.GONE
                camerabtn.visibility=View.VISIBLE
            }

            dialog.show()
        }

        headerView.findViewById<TextView>(R.id.navigationName).text = MyApplication.prefs.getString("worker_name", "")
        headerView.findViewById<TextView>(R.id.navigationEmplId).text = MyApplication.prefs.getString("worker_id", "")

        if(MyApplication.prefs.getString("worker_profile", "").equals("")){
            profileImage.setImageResource(R.drawable.profile_default)
        }
        else{
            val imageBytes = Base64.decode(MyApplication.prefs.getString("worker_profile", ""), 0)
            val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            profileImage.setImageBitmap(image)
        }

        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val date = year.toString()+"-"+(month + 1).toString()+"-"+dayOfMonth.toString()

            // Volley를 이용한 http 통신
            val calendaruploadRequest = @RequiresApi(Build.VERSION_CODES.M)
            object : StringRequest(
                Request.Method.POST,
                "http://ec2-15-165-242-180.ap-northeast-2.compute.amazonaws.com/read_schedule.php",
                Response.Listener<String>{ response ->

                    val jsonObject : JSONObject = JSONObject(response)
                    val array = jsonObject.getJSONArray("response")

                    if(array.length() == 0){
                        binding.scheduleLayout.visibility = View.GONE
                    }
                    else{
                        binding.scheduleLayout.removeAllViews()

                        for (i in 0 until array.length()) {
                            val textView = TextView(this)
                            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            layoutParams.setMargins(130, 40, 0, 0)
                            textView.layoutParams = layoutParams
                            textView.text = array.getJSONObject(i).getString("schedule_contents")
                            textView.setTextAppearance(R.style.calendar_text)
                            binding.scheduleLayout.addView(textView)

                            val drawable = resources.getDrawable(R.drawable.calendar_point)
                            val imageView= ImageView(this)
                            val layoutParams2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            layoutParams2.setMargins(50, -40, 0, 0)
                            imageView.layoutParams = layoutParams2
                            imageView.setImageDrawable(drawable)
                            binding.scheduleLayout.addView(imageView)

                        }
                        binding.scheduleLayout.visibility = View.VISIBLE
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                }){
                override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                    val params : MutableMap<String, String> = HashMap()
                    params["date"] = date

                    return params
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(calendaruploadRequest)
        }
     }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) return true

        return when (item.itemId) {
            R.id.action_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.home,menu)
        return true
    }

    private fun resize(bm: Bitmap): Bitmap? {
        var bm: Bitmap? = bm
        val config: Configuration = resources.configuration
        if (config.smallestScreenWidthDp >= 800) {
            bm = Bitmap.createScaledBitmap(bm!!, 400, 240, true)
        } else if (config.smallestScreenWidthDp >= 600) {
            bm = Bitmap.createScaledBitmap(bm!!, 300, 180, true)
        } else if(config.smallestScreenWidthDp >= 400){
            bm = Bitmap.createScaledBitmap(bm!!, 200, 120, true)
        } else if(config.smallestScreenWidthDp >= 360){
            bm = Bitmap.createScaledBitmap(bm!!, 180, 108, true)
        } else{
            bm = Bitmap.createScaledBitmap(bm!!, 160, 96, true)
        }
        return bm
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            val headerView = binding.calendarDrawerView.getHeaderView(0)
            val editbtn=headerView.findViewById<ImageButton>(R.id.navigationEditBtn)
            val camerabtn=headerView.findViewById<ImageButton>(R.id.navigationCameraBtn)
            val savebtn=headerView.findViewById<ImageButton>(R.id.navigationSaveBtn)
            val edittext=headerView.findViewById<TextView>(R.id.edittext)
            val savetext=headerView.findViewById<TextView>(R.id.savetext)
            val profileImage=headerView.findViewById<ImageView>(R.id.navigationProfile)

            camerabtn.visibility=View.INVISIBLE
            editbtn.visibility=View.VISIBLE
            savebtn.visibility=View.INVISIBLE
            savetext.visibility=View.INVISIBLE
            edittext.visibility=View.VISIBLE

            if(MyApplication.prefs.getString("worker_profile", "").equals("")){
                profileImage.setImageResource(R.drawable.profile_default)
            }
            else{
                val imageBytes = Base64.decode(MyApplication.prefs.getString("worker_profile", ""), 0)
                val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImage.setImageBitmap(image)
            }
        } else {
            super.onBackPressed()
        }
    }
}