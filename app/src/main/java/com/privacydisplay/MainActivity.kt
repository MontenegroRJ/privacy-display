package com.privacydisplay

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var toggle: Switch
    private lateinit var intensityLabel: TextView
    private lateinit var sizeLabel: TextView
    private var updatingUi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(48), dp(24), dp(24))
        }
        setContentView(ScrollView(this).apply { addView(root) })

        root.addView(TextView(this).apply {
            text = "Privacy Display"
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
        })

        toggle = Switch(this).apply {
            text = "Ativar proteção"
            textSize = 18f
            setPadding(0, dp(24), 0, dp(24))
            setOnCheckedChangeListener { _, checked -> if (!updatingUi) onToggle(checked) }
        }
        root.addView(toggle)

        intensityLabel = TextView(this).apply {
            text = "Intensidade: ${Prefs.intensity(this@MainActivity)}%"
            textSize = 16f
        }
        root.addView(intensityLabel)
        root.addView(SeekBar(this).apply {
            max = 90
            progress = Prefs.intensity(this@MainActivity) - 10
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    val v = p + 10
                    intensityLabel.text = "Intensidade: $v%"
                    if (fromUser) {
                        Prefs.setIntensity(this@MainActivity, v)
                        pushUpdate()
                    }
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        sizeLabel = TextView(this).apply {
            text = "Tamanho das listras: ${Prefs.size(this@MainActivity)} dp"
            textSize = 16f
            setPadding(0, dp(24), 0, 0)
        }
        root.addView(sizeLabel)
        root.addView(SeekBar(this).apply {
            max = 9
            progress = Prefs.size(this@MainActivity) - 1
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    val v = p + 1
                    sizeLabel.text = "Tamanho das listras: $v dp"
                    if (fromUser) {
                        Prefs.setSize(this@MainActivity, v)
                        pushUpdate()
                    }
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        root.addView(TextView(this).apply {
            text = "Observação: o efeito é feito por software (listras sobre a tela). " +
                "Ele dificulta a leitura de quem olha de lado, mas não substitui " +
                "uma película de privacidade de verdade."
            textSize = 13f
            alpha = 0.7f
            setPadding(0, dp(32), 0, 0)
        })
    }

    override fun onResume() {
        super.onResume()
        val active = Prefs.enabled(this) && Settings.canDrawOverlays(this)
        setToggle(active)
        if (active) {
            startForegroundService(Intent(this, OverlayService::class.java))
        }
    }

    private fun onToggle(checked: Boolean) {
        if (checked) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Permita \"Sobrepor a outros apps\" e volte aqui", Toast.LENGTH_LONG).show()
                setToggle(false)
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
                return
            }
            if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
            Prefs.setEnabled(this, true)
            startForegroundService(Intent(this, OverlayService::class.java))
        } else {
            Prefs.setEnabled(this, false)
            stopService(Intent(this, OverlayService::class.java))
        }
    }

    private fun pushUpdate() {
        if (Prefs.enabled(this)) {
            startService(Intent(this, OverlayService::class.java).setAction(OverlayService.ACTION_UPDATE))
        }
    }

    private fun setToggle(on: Boolean) {
        updatingUi = true
        toggle.isChecked = on
        updatingUi = false
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
