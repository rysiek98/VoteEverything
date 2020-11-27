package com.example.voteeverything

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_start_screen.*
import kotlinx.android.synthetic.main.sing_in_dialog.view.*
import kotlinx.android.synthetic.main.sing_up_dialog.view.*


class StartScreenActivity : AppCompatActivity(){

    private var controlHideUIFlag = false
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                if(!controlHideUIFlag) {
                    Handler().postDelayed({ hideSystemUI() }, 2000)
                }else{
                    hideSystemUI()
                    controlHideUIFlag = false
                }
            }
            auth = Firebase.auth
        }


        singInSScreenBt.setOnClickListener {
            controlHideUIFlag = true
            val dialogView = LayoutInflater.from(this).inflate(R.layout.sing_in_dialog, null)
            val mBuilder = AlertDialog.Builder(this).setView(dialogView)
            val mAlert = mBuilder.show()

            mAlert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogView.loginSingInBt.setOnClickListener {
                    val password = dialogView.passwordFromUser.text.toString()
                    val emial = dialogView.emailFromUser.text.toString()
                    if (password.isNotEmpty() && emial.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(emial,password)
                        .addOnCompleteListener(this) { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(baseContext,
                                    "Authentication failed. Wrong address e-mail or password.",
                                    Toast.LENGTH_SHORT).show()
                            }else{
                                openMainWindow()
                                mAlert.dismiss()
                            }
                        }
                    }else{
                        Toast.makeText(baseContext,
                            "Oh no! You forgot to enter e-mail & password.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            dialogView.backSingInBt.setOnClickListener {
                mAlert.dismiss()
            }
        }

        singUpSScreenBt.setOnClickListener {
            controlHideUIFlag = true
            val dialogView = LayoutInflater.from(this).inflate(R.layout.sing_up_dialog, null)
            val mBuilder = AlertDialog.Builder(this).setView(dialogView)
            val mAlert = mBuilder.show()

            mAlert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogView.createSingUpBt.setOnClickListener {
                val password = dialogView.passwordFromUserSingUp.text.toString()
                val emial = dialogView.emailFromUserSingUp.text.toString()
                if (isPasswordValid(password) && isEmailValid(emial)){
                    auth.createUserWithEmailAndPassword(emial, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = dialogView.nicknameFormUserSingUp.text.toString()
                                }
                                user!!.updateProfile(profileUpdates)
                                    .addOnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            Toast.makeText(baseContext, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show()
                                        }else{
                                            openMainWindow()
                                            mAlert.dismiss()
                                        }
                                    }
                            }
                        }
            }else{
                    Toast.makeText(
                        baseContext, "Wrong password length or wrong e-mial address. Your password should have 6 characters or more.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            dialogView.backSingUpBt.setOnClickListener {
                mAlert.dismiss()
            }
        }

        guestSScreenBt.setOnClickListener {
            openMainWindow()
        }

        exitSScreenBt.setOnClickListener {
            finish()
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    //Simply password validation fun
    fun isPasswordValid(password: String): Boolean{
        return if(password.isNotEmpty()) {
            password.length > 5
        }else{
            false
        }
    }
    //E-mail validation fun
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun openMainWindow(){
        val mainWindow = Intent(applicationContext,MainWindowActivity::class.java)
        startActivity(mainWindow)
    }

}