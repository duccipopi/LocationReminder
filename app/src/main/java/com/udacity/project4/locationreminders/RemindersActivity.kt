package com.udacity.project4.locationreminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationController
import com.udacity.project4.authentication.Authenticator
import kotlinx.android.synthetic.main.activity_reminders.*
import org.koin.android.ext.android.inject

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSION_REQUEST_CODE = 0x1010;
    }

    private val authController: Authenticator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        authController.getLoginState().observe(this, Observer { logged ->
            logged?.let {
                if (!logged) {
                    authController.signIn(this)
                    finish()
                }
            }
        })

        if (!allPermissionsGranted()) {
            requestMissingPermissions()
        }

    }

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestMissingPermissions() {
        requestPermissions(
            (permissions.filter {
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }).toTypedArray(),
        REQUEST_PERMISSION_REQUEST_CODE)
    }

    private fun allPermissionsGranted(): Boolean {
        return permissions.none { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
