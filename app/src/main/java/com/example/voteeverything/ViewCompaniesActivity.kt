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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_companies)
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                if (!controlHideUIFlag) {
                    Handler().postDelayed({ hideSystemUI() }, 2000)
                } else {
                    hideSystemUI()
                    controlHideUIFlag = false
                }
            }
        }

       backVCompanyBt.setOnClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        val docRefSurveys = db.collection("dbInfo").document("companies")
        paintCompanies(docRefSurveys)

    }

    private fun paintCompanies(
        docRefCompanies: DocumentReference) {
        val container = findViewById<View>(R.id.companyContainer) as TableLayout
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
        container: TableLayout) {
        val rateCompanyWindow = Intent(applicationContext,RateCompaniesActivity::class.java)
        val text = "Ups... Nobody haven't created any surveys yet. Let's make first survey!"


        if(companyToUser.size == 0){
            createText(container, text)
            return
        }

        (1 until companyToUser.size step 2).forEach { i ->
            val userUID = companyToUser[i - 1]
            val title = companyToUser[i]
                val newElement = createNewElement(container, title)
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
            db.collection(userUID).document(title).get()
                .addOnSuccessListener { DocumentSnapshot ->
                    if (DocumentSnapshot.exists()) {
                        rateCompanyWindow.putExtra("title", title)
                        rateCompanyWindow.putExtra("user", userUID)
                        rateCompanyWindow.putExtra("currentUser", currentUserUID)
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

    private fun createNewElement(container: TableLayout, title: String): MaterialButton{
        val params: TableRow.LayoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val newElement = MaterialButton(this)
        newElement.text = title
        newElement.id = container.size + 1
        newElement.setBackgroundColor(Color.BLACK)
        newElement.setTextColor(Color.WHITE)
        newElement.cornerRadius = 20
        newElement.layoutParams = params
        container.addView(newElement)
        return newElement
    }

    private fun createText(container: TableLayout, text: String) {
        val newElement = TextView(this)
        newElement.text = text
        newElement.textSize = 30f
        newElement.setTextColor(Color.WHITE)
        container.addView(newElement)
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
}