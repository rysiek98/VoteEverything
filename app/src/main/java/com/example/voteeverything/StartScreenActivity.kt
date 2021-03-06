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
                    Handler().postDelayed({ hideSystemUI(window) }, 2000)
                }else{
                    hideSystemUI(window)
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
                    val email = dialogView.emailFromUser.text.toString()
                    if (password.isNotEmpty() && email.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(this) { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(baseContext,
                                    "Authentication failed. Wrong address e-mail or password.",
                                    Toast.LENGTH_SHORT).show()
                            }else{
                                openMainWindow(false)
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
                val email = dialogView.emailFromUserSingUp.text.toString()
                if (isPasswordValid(password) && isEmailValid(email)){
                    auth.createUserWithEmailAndPassword(email, password)
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
                                            openMainWindow(false)
                                            mAlert.dismiss()
                                        }
                                    }
                            }
                        }
            }else{
                    Toast.makeText(
                        baseContext, "Wrong password length or wrong e-mail address. Your password should have 6 characters or more.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            dialogView.backSingUpBt.setOnClickListener {
                mAlert.dismiss()
            }
        }

        guestSScreenBt.setOnClickListener {
            openMainWindow(true)
        }

        exitSScreenBt.setOnClickListener {
            finish()
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    //Simply password validation fun
    private fun isPasswordValid(password: String): Boolean{
        return if(password.isNotEmpty()) {
            password.length > 5
        }else{
            false
        }
    }
    //E-mail validation fun
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun openMainWindow(flag: Boolean){
        if(flag) {
            val mainWindow = Intent(applicationContext, MainWindowActivity::class.java)
            mainWindow.putExtra("userIsAnonymous", true)
            startActivity(mainWindow)
        }else{
            val mainWindow = Intent(applicationContext, MainWindowActivity::class.java)
            mainWindow.putExtra("userIsAnonymous", false)
            startActivity(mainWindow)
        }
    }

}