package com.udacity.project4.authentication

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationController(private val loginActivity: Class<*>) {
    private var firebaseAuth = FirebaseAuth.getInstance()

    init {
        firebaseAuth.addAuthStateListener { newAuth ->
            _logged.value = newAuth.currentUser != null
        }
    }

    val logged: LiveData<Boolean>
        get() = _logged

    private val _logged: MutableLiveData<Boolean> =
        MutableLiveData(firebaseAuth.currentUser != null)

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun signIn(context: Context) {
        context.startActivity(Intent(context, loginActivity))
    }

}