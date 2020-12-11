package com.example.voteeverything

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_company.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddCompanyActivity : AppCompatActivity() {

    private var controlHideUIFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_company)
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

        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        val docRefCompanies = db.collection("dbInfo").document("companies")

        backACompanyBt.setOnClickListener {
            finish()
        }

        createACompanyBt.setOnClickListener {

            val title =  companyNameACompany.text.toString()
            val description = descriptionACompany.text.toString()
            var companies: ArrayList<String> = ArrayList()


            //Adding company
            if (title.isNotEmpty() && description.isNotEmpty()) {

                val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
                val currentDate = sdf.format(Date())
                val company = hashMapOf(
                    "userID" to user?.uid,
                    "userName" to user?.displayName,
                    "creationData" to currentDate,
                    "title" to title,
                    "description" to description
                )

                db.collection("companies").document(user?.uid.toString())
                    .set(company)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(baseContext,"Object successfully add to database.", Toast.LENGTH_SHORT)
                            .show()
                        Handler().postDelayed({ resetUI() }, 500)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(baseContext,"Failure!", Toast.LENGTH_LONG)
                            .show()
                    }

                //Adding uid-company to uid-company
                val user = user?.uid.toString()
                docRefCompanies.get()
                    .addOnSuccessListener { DocumentSnapshot->
                        if(DocumentSnapshot.exists()){
                            if(DocumentSnapshot.contains("companyToUser")) {
                                companies = DocumentSnapshot.get("companyToUser") as ArrayList<String>
                            }
                            companies.add(user)
                            companies.add(title)
                            updateCompany(companies, db)
                        }else{
                            companies.add(user)
                            companies.add(title)
                            updateCompany(companies, db)
                        }
                    }
                    .addOnFailureListener {
                        companies.add(user)
                        companies.add(title)
                        updateCompany(companies, db)
                    }

            }else{
                Toast.makeText(baseContext,"Please enter all necessary data.", Toast.LENGTH_SHORT)
                    .show()
            }

        }

    }

    private fun updateCompany(companies: java.util.ArrayList<String>, db: FirebaseFirestore) {
        val data   = hashMapOf(
            "companyToUser" to companies
        )
        db.collection("dbInfo").document("companies")
            .update(data as Map<String, Any>)
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

    private fun resetUI() {
        companyNameACompany.text.clear()
        descriptionACompany.text.clear()
    }

}