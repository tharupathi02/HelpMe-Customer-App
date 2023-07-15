package com.leoxtech.customerapp.Screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.leoxtech.customerapp.R
import com.leoxtech.customerapp.databinding.ActivityDoneScreenBinding

class ActivityDoneScreen : AppCompatActivity() {

    private lateinit var binding: ActivityDoneScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoneScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titleText = intent.getStringExtra("titleText")
        val subTitleText = intent.getStringExtra("subTitleText")
        val buttonText = intent.getStringExtra("buttonText")
        val activity = intent.getStringExtra("activity")

        binding.txtTitle.text = titleText
        binding.txtSubTitle.text = subTitleText
        binding.btnNext.text = buttonText

        binding.btnNext.setOnClickListener {
            // change activity depending on the activity name passed from the previous activity (intent)
            when (activity) {
                "MainActivity" -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        }

    }
}