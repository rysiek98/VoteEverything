package com.example.voteeverything

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
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
                    Handler().postDelayed({ hideSystemUI(window) }, 2000)
                } else {
                    hideSystemUI(window)
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

            val title = companyNameACompany.text.toString()
            val description = descriptionACompany.text.toString()
            val votes: ArrayList<Int> = ArrayList()
            votes.addAll(listOf(0, 0))
            val voters: ArrayList<String> = ArrayList()
            val comments: ArrayList<String> = ArrayList()

            db.collection(user?.uid.toString()).document("C_" + title).get()
                .addOnSuccessListener { DocumentSnapshot ->
                    if (DocumentSnapshot.exists()) {
                        Toast.makeText(
                            baseContext,
                            "You already have company with that name!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {

                        //Adding company
                        if (title.isNotEmpty() && description.isNotEmpty()) {
                            addCompany(title, description, db, user, votes, voters, comments)
                            addCompanyToUser(title, db, user, docRefCompanies)

                        } else {
                            Toast.makeText(
                                baseContext,
                                "Please enter all necessary data.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                    }
                }
        }
    }

    private fun updateCompany(companies: java.util.ArrayList<String>, db: FirebaseFirestore) {
        val data = hashMapOf(
            "companyToUser" to companies
        )
        db.collection("dbInfo").document("companies")
            .update(data as Map<String, Any>)
    }

    private fun setCompany(companies: java.util.ArrayList<String>, db: FirebaseFirestore) {
        val data = hashMapOf(
            "companyToUser" to companies
        )
        db.collection("dbInfo").document("companies")
            .set(data)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun resetUI() {
        companyNameACompany.text.clear()
        descriptionACompany.text.clear()
    }

    private fun addCompany(
        title: String,
        description: String,
        db: FirebaseFirestore,
        user: FirebaseUser?,
        votes: ArrayList<Int>,
        voters: ArrayList<String>,
        comments: ArrayList<String>
    ) {
        //Adding company
        if (title.isNotEmpty() && description.isNotEmpty()) {

            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            val company = hashMapOf(
                "userID" to user?.uid,
                "userName" to user?.displayName,
                "creationData" to currentDate,
                "title" to title,
                "description" to description,
                "votes" to votes,
                "voters" to voters,
                "comments" to comments
            )

            db.collection(user?.uid.toString()).document("C_" + title)
                .set(company)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        baseContext,
                        "Successfully add to database.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Handler().postDelayed({ resetUI() }, 500)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(baseContext, "Failure!", Toast.LENGTH_LONG)
                        .show()
                }

        }

    }

    private fun addCompanyToUser(
        title: String,
        db: FirebaseFirestore,
        user: FirebaseUser?,
        docRefCompanies: DocumentReference){
        //Adding uid-company to uid-company
        val user = user?.uid.toString()
        var companies: ArrayList<String> = ArrayList()

        docRefCompanies.get()
            .addOnSuccessListener { DocumentSnapshot ->
                if (DocumentSnapshot.exists()) {
                    if (DocumentSnapshot.contains("companyToUser")) {
                        companies =
                            DocumentSnapshot.get("companyToUser") as ArrayList<String>
                    }
                    companies.add(user)
                    companies.add(title)
                    updateCompany(companies, db)
                } else {
                    //If field companyToUser does't exist fun setCompany create it and add data
                    companies.add(user)
                    companies.add(title)
                    setCompany(companies, db)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    baseContext,
                    "Failed to connect to the database",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

}