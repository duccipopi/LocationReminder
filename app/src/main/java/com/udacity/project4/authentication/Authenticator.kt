package com.udacity.project4.authentication

import android.content.Context
import androidx.lifecycle.LiveData

interface Authenticator {
    fun signIn(context: Context)
    fun signOut()
    fun getLoginState(): LiveData<Boolean>
}