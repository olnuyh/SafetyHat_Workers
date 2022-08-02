package com.example.workers

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class MyApplication : Application() {
    companion object{
        lateinit var prefs : SharedPreferencesManager
    }

    override fun onCreate() {
        prefs = SharedPreferencesManager(applicationContext)
        super.onCreate()
    }
}

class SharedPreferencesManager(context : Context){
    private val prefs = context.getSharedPreferences("worker", Context.MODE_PRIVATE)

    fun getString(key:String, value:String) : String{
        return prefs.getString(key, value).toString()
    }

    fun setString(key:String, value:String) {
        prefs.edit().putString(key, value).apply()
    }

    fun clear(){
        prefs.edit().clear().commit()
    }
}