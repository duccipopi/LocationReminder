package com.udacity.project4.authentication

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationController(private val loginActivity: Class<*>): Authenticator {
    private var firebaseAuth = FirebaseAuth.getInstance()

    init {
        firebaseAuth.addAuthStateListener { newAuth ->
            _logged.value = newAuth.currentUser != null
        }
    }

    private val _logged: MutableLiveData<Boolean> =
        MutableLiveData(firebaseAuth.currentUser != null)

    override fun getLoginState(): LiveData<Boolean> {
        return _logged
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun signIn(context: Context) {
        context.startActivity(Intent(context, loginActivity))
    }

}