package com.example.project_2_ecommerce_app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.finmate.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel() {
    private  val auth = Firebase.auth
    private val firestore = Firebase.firestore
    fun Login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onResult(true, null.toString())
                } else {
                    onResult(false, it.exception?.localizedMessage ?: "Unknown Error")
                }
            }


    }
    fun Signup(email : String , name: String, password: String, onResult: (Boolean,String) -> Unit){

        //create a user with email and password
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val userId = it.result?.user?.uid
                    val user = User(name,email,userId!!)
//                    now we will add the data to firebase
                    firestore.collection("users").document(userId).set(user)
                        .addOnCompleteListener{
                                dbTask ->
                            if(dbTask.isSuccessful){
                                onResult(true, null.toString())
                            }
                            else{
                                onResult(false,dbTask.exception?.localizedMessage ?: "Something went wrong")
                            }


                        }

                }
                else{
                    onResult(false,it.exception?.localizedMessage ?: "Unknown Error")
                }
            }

    }


}