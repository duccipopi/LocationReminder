package com.udacity.project4.locationreminders.savereminder

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    private val REQUEST_CHECK_SETTINGS: Int = 0x5e7

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            tryToSaveReminder()
        }
    }

    private fun tryToSaveReminder() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminder = ReminderDataItem(
            title, description, location, latitude, longitude
        )

        if (allPermissionsGranted(LOCATION_BACKGROUND_PERMISSIONS)
            && anyPermissionsGranted(LOCATION_FOREGROUND_PERMISSIONS)
        ) {

            processIfDeviceLocationIsOn {
                addGeofence(requireContext(), reminder)
                _viewModel.validateAndSaveReminder(reminder)
            }

        } else {
            val permission = LOCATION_BACKGROUND_PERMISSIONS + LOCATION_FOREGROUND_PERMISSIONS
            requestMissingPermissions(permission)
        }
    }

    private fun processIfDeviceLocationIsOn(function: () -> Unit) {

        val builder =
            LocationSettingsRequest.Builder().addLocationRequest(
                LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_LOW_POWER
                }
            )

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    startIntentSenderForResult(
                        exception.resolution.intentSender, REQUEST_CHECK_SETTINGS,
                        null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

        task.addOnCompleteListener {
            if (it.isSuccessful) function()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_REQUEST_CODE) {
            val foreground = permissions.filter { LOCATION_FOREGROUND_PERMISSIONS.contains(it) }
            val anyForeground =
                foreground.isEmpty() || foreground.any { grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_GRANTED }

            val background = permissions.filter { LOCATION_BACKGROUND_PERMISSIONS.contains(it) }
            val allBackground =
                background.isEmpty() || background.all { grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_GRANTED }

            if (anyForeground && allBackground) {
                tryToSaveReminder()
            } else {
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation_save
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                tryToSaveReminder()
            } else {
                _viewModel.showSnackBarInt.value = R.string.location_required_error
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
