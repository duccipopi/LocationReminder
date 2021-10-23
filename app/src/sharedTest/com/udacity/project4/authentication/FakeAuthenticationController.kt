package com.udacity.project4.authentication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FakeAuthenticationController(private val logged: Boolean = true): Authenticator {
    private val _logged: MutableLiveData<Boolean> =
        MutableLiveData(logged)

    override fun signIn(context: Context) {
        _logged.value = true
    }

    override fun signOut() {
        _logged.value = false
    }

    override fun getLoginState(): LiveData<Boolean> {
        return _logged
    }
}