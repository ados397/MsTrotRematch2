package com.ados.mstrotrematch2

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

private const val FILENAME = "prefs"
private const val PREF_TEXT = "Text"
class MySharedPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(FILENAME, 0)
    var text: String?
        get() = prefs.getString(PREF_TEXT, "")
        set(value) = prefs.edit().putString(PREF_TEXT, value).apply()
}

class App : Application() {
    init {
        INSTANCE = this
    }

    override fun onCreate() {
        prefs = MySharedPreferences(applicationContext)
        super.onCreate()
    }

    companion object {
        lateinit var INSTANCE: App
        lateinit var prefs: MySharedPreferences
    }
}