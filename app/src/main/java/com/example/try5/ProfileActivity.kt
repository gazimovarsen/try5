package com.example.try5

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.try5.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_profile.*

//class User{
//    lateinit var age: String
//    lateinit var email: String
//    lateinit var latitude: String
//    lateinit var longitude: String
//    lateinit var name: String
//    var online: Boolean = false
//    var signed: Boolean = false
//}

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = Firebase.storage

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }
        binding.mapBtn.setOnClickListener {
            goToMap()
        }
        binding.swipesBtn.setOnClickListener {
            goToSwipes()
        }
        val currentEmail = firebaseAuth.currentUser?.email.toString()
        val db = FirebaseFirestore.getInstance()
        var pass = false
        Log.d("TAG", "func started")
        db.collection("users").get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result!!) {
                    if (document.data.getValue("email") == currentEmail) {
                        pass = document.data.getValue("signed") as Boolean
                    }
                }
            }
        }.addOnCompleteListener {
        if (pass) {
            binding.nameRegister.visibility = View.GONE
            binding.ageRegister.visibility = View.GONE
            binding.buttonRegister.visibility = View.GONE
            binding.profileName.visibility = View.VISIBLE
            binding.mapBtn.visibility = View.VISIBLE
            binding.swipesBtn.visibility = View.VISIBLE

            binding.interestRegister.visibility = View.GONE
            binding.sexRegisterSwitch.visibility = View.GONE
        } else {
            binding.nameRegister.visibility = View.VISIBLE
            binding.ageRegister.visibility = View.VISIBLE
            binding.buttonRegister.visibility = View.VISIBLE
            binding.profileName.visibility = View.GONE
            binding.mapBtn.visibility = View.GONE
            binding.swipesBtn.visibility = View.GONE

            binding.interestRegister.visibility = View.VISIBLE
            binding.sexRegisterSwitch.visibility = View.VISIBLE

            buttonRegister.setOnClickListener {
                val name = binding.nameRegister.text.toString()
                val age = binding.ageRegister.text.toString()
                val interest = binding.interestRegister.text.toString()
                val sexState = binding.sexRegisterSwitch.isChecked
                val sex = if(sexState){
                    "female"
                }else{
                    "male"
                }

                if (emailTaken(currentEmail)) {
                    saveFireStore(name, age, sex, interest, currentEmail, true)
                    binding.nameRegister.visibility = View.GONE
                    binding.ageRegister.visibility = View.GONE
                    binding.buttonRegister.visibility = View.GONE
                    binding.profileName.visibility = View.VISIBLE
                    binding.mapBtn.visibility = View.VISIBLE
                    binding.swipesBtn.visibility = View.VISIBLE

                    binding.interestRegister.visibility = View.GONE
                    binding.sexRegisterSwitch.visibility = View.GONE
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "This email already has an account!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        }
    }

    private fun saveFireStore(name: String, age: String, sex: String, interest: String, email: String, signed: Boolean = false) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "age" to age,
            "name" to name,
            "sex" to sex,
            "email" to email,
            "latitude" to "0",
            "longitude" to "0",
            "signed" to signed,
            "online" to true,
            "interest" to interest
        )
        db.collection("users").add(user)
            .addOnSuccessListener {
                Toast.makeText(this@ProfileActivity, "Record added successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this@ProfileActivity, "Record is not added. Something went wrong!", Toast.LENGTH_SHORT).show()

            }
    }

    private fun emailTaken(email: String): Boolean {
        val db = FirebaseFirestore.getInstance()
        var result = true
        db.collection("users").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(document in it.result!!){
                    if(document.data.getValue("email") == email){
                        result = false
                    }
                }
            }
        }
        return result
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null){
            val email = firebaseUser.email
            binding.emailTv.text = email
            binding.profileName.text = "Hello there, $email"
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun goToMap() {
        startActivity(Intent(this, MapActivity::class.java))
        finish()
    }

    private fun goToSwipes(){
        startActivity(Intent(this, SwipesActivity::class.java))
    }
}