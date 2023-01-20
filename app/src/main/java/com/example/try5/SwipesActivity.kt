package com.example.try5

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.try5.databinding.ActivitySwipesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception

class SwipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySwipesBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()

        val list = db.collection("users")
        list.get().addOnSuccessListener { documentSnapshots ->
            val k = 0
            val firstPage = documentSnapshots.documents[k]
            if(firstPage != null){
                nextPage(0)
            }
        }

        val file = "text.txt"
        val data = "asd"
        try {
            val fileOS: FileOutputStream = openFileOutput(file, Context.MODE_PRIVATE)
            fileOS.write(data.toByteArray())
            Log.d("ASDASDASD", "complete")
        }
        catch(e: FileNotFoundException){
            e.printStackTrace()
        }
        catch(e: Exception){
            e.printStackTrace()
        }
    }

    private fun nextPage(iterator: Int) {
        val currentUser = firebaseAuth.currentUser
        val db = FirebaseFirestore.getInstance()
        val list = db.collection("users")
        list.get().addOnSuccessListener { documentSnapshots ->
            var k = iterator
            val size = documentSnapshots.size()

            var currentPage = documentSnapshots.documents[0]
            if(k < size){
                currentPage = documentSnapshots.documents[k]
            }else{
                return@addOnSuccessListener nextPage(0)
            }

            if (currentUser != null) {
                if(currentPage.get("email") != currentUser.email){
                    binding.swipesName.text = currentPage.data?.getValue("name").toString()
                    binding.swipesAge.text = currentPage.data?.getValue("age").toString()
                    binding.swipesIntrests.text = currentPage.data?.getValue("interest").toString()
                }

                k += 1

                binding.buttonBust.setOnClickListener{
                    Log.d("TAG", "BUST")
                    return@setOnClickListener nextPage(k)
                }

                binding.buttonLike.setOnClickListener{
                    Log.d("TAG", "LIKE")
                    return@setOnClickListener nextPage(k)
                }
            }else{
                return@addOnSuccessListener nextPage(0)
            }
        }
    }
}