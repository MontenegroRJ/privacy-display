package com.privacydisplay

import android.content.Context

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("privacy", Context.MODE_PRIVATE)

    fun enabled(c: Context) = sp(c).getBoolean("enabled", false)
    fun setEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("enabled", v).apply()

    // 10..100 (opacidade das listras, em %)
    fun intensity(c: Context) = sp(c).getInt("intensity", 60)
    fun setIntensity(c: Context, v: Int) = sp(c).edit().putInt("intensity", v).apply()

    // 1..10 (espessura das listras, em dp)
    fun size(c: Context) = sp(c).getInt("size", 2)
    fun setSize(c: Context, v: Int) = sp(c).edit().putInt("size", v).apply()
}
