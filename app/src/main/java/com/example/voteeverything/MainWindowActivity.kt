package com.example.voteeverything

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_window.*


class MainWindowActivity : AppCompatActivity() {

    private var controlHideUIFlag = false
    private var userIsAnonymous = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                if (!controlHideUIFlag) {
                    Handler().postDelayed({ hideSystemUI(window) }, 2000)
                } else {
                    hideSystemUI(window)
                    controlHideUIFlag = false
                }
            }
        }

        userIsAnonymous = intent.getBooleanExtra("userIsAnonymous", true)

        availableCompanyMWindowBt.setOnClickListener {
            val rateCompanyWindow = Intent(applicationContext,ViewCompaniesActivity::class.java)
            rateCompanyWindow.putExtra("userIsAnonymous", userIsAnonymous)
            startActivity(rateCompanyWindow)
        }

        addCompanyMWindowBt.setOnClickListener {
            if (!userIsAnonymous) {
                val addCompanyWindow = Intent(applicationContext, AddCompanyActivity::class.java)
                startActivity(addCompanyWindow)
            }else{
                Toast.makeText(baseContext,
                    "Only registered user could use this functionality.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        availableSurveysMWindowBt.setOnClickListener {
            val availableSurveyWindow = Intent(applicationContext,ViewSurveysActivity::class.java)
            availableSurveyWindow.putExtra("flag", false)
            availableSurveyWindow.putExtra("userIsAnonymous", userIsAnonymous)
            startActivity(availableSurveyWindow)
        }

        createSurveyMWindowBt.setOnClickListener {
            if (!userIsAnonymous) {
            val createSurveyWindow = Intent(applicationContext,CreateSurveyActivity::class.java)
            startActivity(createSurveyWindow)
            }else{
                Toast.makeText(baseContext,
                    "Only registered user could use this functionality.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        logoutMWindowBt.setOnClickListener {
            Firebase.auth.signOut()
            finish()
        }

        userProfMWindowBt.setOnClickListener {
            if (!userIsAnonymous) {
                val userWindow = Intent(applicationContext,UserActivity::class.java)
                startActivity(userWindow)
            }else{
                Toast.makeText(baseContext,
                    "Only registered user could use this functionality.",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

}