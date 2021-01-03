package com.example.voteeverything

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_view_companies.*


class ViewCompaniesActivity : AppCompatActivity() {

    private var controlHideUIFlag = false
    private val db = Firebase.firestore
    private val currentUserUID = Firebase.auth.currentUser?.uid.toString()
    private var userIsAnonymous = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_companies)
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

       backVCompanyBt.setOnClickListener {
            finish()
        }

        userIsAnonymous = intent.getBooleanExtra("userIsAnonymous", true)
    }

    override fun onResume() {
        super.onResume()
        val docRefSurveys = db.collection("dbInfo").document("companies")
        paintCompanies(docRefSurveys)

    }

    private fun paintCompanies(
        docRefCompanies: DocumentReference) {
        val container = findViewById<View>(R.id.companyContainer) as LinearLayout
        container.removeAllViews()
        var companyToUser: ArrayList<String>
        docRefCompanies.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    companyToUser = DocumentSnapshot.get("companyToUser") as ArrayList<String>
                        paintCompany(companyToUser, container)
                }else{
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun paintCompany(
        companyToUser: java.util.ArrayList<String>,
        container: LinearLayout) {
        val rateCompanyWindow = Intent(applicationContext,RateCompaniesActivity::class.java)
        val text = "Ups... Nobody haven't add anything yet. Let's add first item!"


        if(companyToUser.size == 0){
            createText(container, text, this)
            return
        }

        (1 until companyToUser.size step 2).forEach { i ->
            val userUID = companyToUser[i - 1]
            val title = companyToUser[i]
                val newElement = createNewElement(container, title, this)
                activeButton(newElement, userUID, title, rateCompanyWindow)
        }

    }

    private fun activeButton(
        newElement: MaterialButton,
        userUID: String,
        title: String,
        rateCompanyWindow: Intent,
    ) {
        newElement.setOnClickListener {
            db.collection(userUID).document("C_"+title).get()
                .addOnSuccessListener { DocumentSnapshot ->
                    if (DocumentSnapshot.exists()) {
                        rateCompanyWindow.putExtra("title", title)
                        rateCompanyWindow.putExtra("user", userUID)
                        rateCompanyWindow.putExtra("currentUser", currentUserUID)
                        rateCompanyWindow.putExtra("userIsAnonymous", userIsAnonymous)
                        startActivity(rateCompanyWindow)
                    } else {
                        Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

}