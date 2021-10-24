package com.udacity.project4.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.Fragment

const val REQUEST_PERMISSION_REQUEST_CODE = 0x1010;

val LOCATION_FOREGROUND_PERMISSIONS = listOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

val LOCATION_BACKGROUND_PERMISSIONS =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    else emptyList()

fun Activity.requestMissingPermissions(
    permissions: List<String>,
    requestCode: Int = REQUEST_PERMISSION_REQUEST_CODE
) {
    requestPermissions(
        (permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }).toTypedArray(),
        requestCode
    )
}

fun Fragment.requestMissingPermissions(
    permissions: List<String>,
    requestCode: Int = REQUEST_PERMISSION_REQUEST_CODE
) {
    requestPermissions(
        (permissions.filter {
            requireActivity().checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }).toTypedArray(),
        requestCode
    )
}

fun Activity.allPermissionsGranted(permissions: List<String>): Boolean {
    return permissions.none { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
}

fun Fragment.allPermissionsGranted(permissions: List<String>): Boolean {
    return requireActivity().allPermissionsGranted(permissions)
}

fun Activity.anyPermissionsGranted(permissions: List<String>): Boolean {
    return permissions.any { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
}

fun Fragment.anyPermissionsGranted(permissions: List<String>): Boolean {
    return requireActivity().anyPermissionsGranted(permissions)
}