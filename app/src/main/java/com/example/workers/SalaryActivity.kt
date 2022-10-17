package com.example.workers

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.workers.databinding.ActivitySalaryBinding
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SalaryActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var binding : ActivitySalaryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySalaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toggle.syncState()

        binding.salaryDrawerView.setNavigationItemSelectedListener {
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

        val headerView = binding.salaryDrawerView.getHeaderView(0)
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
                    BuildConfig.API_KEY + "update_worker_profile.php",
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

        val month = SimpleDateFormat("M", Locale.KOREA).format(Date())

        requestSalary(month)
        binding.salaryRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        var month_number = month.toInt()

        binding.salaryTitle.text = MyApplication.prefs.getString("worker_name", "") + " 님의 "+ month + "월 급여"

        binding.salaryPrevBtn.setOnClickListener {
            month_number = month_number - 1
            if(month_number == 0){
                month_number = 12
            }
            requestSalary(month_number.toString())
            binding.salaryTitle.text = MyApplication.prefs.getString("worker_name", "") + " 님의 " + month_number.toString()+ "월 급여"
        }

        binding.salaryNextBtn.setOnClickListener {
            month_number = month_number + 1
            if(month_number == 13){
                month_number = 1
            }
            requestSalary(month_number.toString())
            binding.salaryTitle.text = MyApplication.prefs.getString("worker_name", "") + " 님의 " + month_number.toString() + "월 급여"
        }

        binding.downBtn.setOnClickListener {
            binding.salaryRecyclerView.visibility = View.VISIBLE
            binding.downBtn.visibility = View.GONE
            binding.upBtn.visibility = View.VISIBLE
        }
        binding.upBtn.setOnClickListener {
            binding.salaryRecyclerView.visibility = View.GONE
            binding.downBtn.visibility = View.VISIBLE
            binding.upBtn.visibility = View.GONE

        }


    }

    fun requestSalary(month : String){
        val salaryRequest = object : StringRequest( // Volley를 이용한 http 통신
            Request.Method.POST,
            BuildConfig.API_KEY + "worker_salary.php",
            Response.Listener<String> { response ->
                val jsonObject : JSONObject = JSONObject(response)
                val array = jsonObject.getJSONArray("response")

                val time_sum = array.getJSONObject(0).getString("time_sum")

                if(time_sum.equals("null")){
                    binding.workTime.text = "0시간"
                    binding.workSalary.text = "0원"
                }else {
                    val time_sum = array.getJSONObject(0).getString("time_sum")

                    binding.workTime.text = (time_sum.toInt() / 60).toString() + "시간"
                    binding.workSalary.text = array.getJSONObject(0).getString("salary_sum") + "원"
                }

                array.remove(0)

                binding.salaryRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.salaryRecyclerView.adapter = SalaryAdapter(this, array)

            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? { // API로 전달할 데이터
                val params : MutableMap<String, String> = HashMap()
                params["month"] = month
                params["pkey"] = MyApplication.prefs.getString("worker_pkey", "")
                return params
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(salaryRequest)
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

            val headerView = binding.salaryDrawerView.getHeaderView(0)
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