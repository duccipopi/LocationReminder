package com.udacity.project4.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import java.util.concurrent.TimeUnit

const val EXTRA_REMINDER_ID = "extra_reminder_id"
const val ACTION_GEOFENCE_EVENT = "action.geofence.event"
const val GEOFENCE_RADIUS_IN_METERS = 100f
const val GEOFENCE_EXPIRATION = Geofence.NEVER_EXPIRE

fun getGeofencePendingIntent(context: Context, reminderId: String): PendingIntent {
    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    intent.action = ACTION_GEOFENCE_EVENT
    intent.putExtra(EXTRA_REMINDER_ID, reminderId)
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
}

@SuppressLint("MissingPermission")
fun addGeofence(context: Context, reminder: ReminderDataItem): Boolean {

    if (reminder.latitude == null || reminder.longitude == null)
        return false

    val geofencingClient = LocationServices.getGeofencingClient(context)

    val geofence = Geofence.Builder()
        .setRequestId(reminder.id)
        .setCircularRegion(
            reminder.latitude!!,
            reminder.longitude!!,
            GEOFENCE_RADIUS_IN_METERS
        )
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .setExpirationDuration(GEOFENCE_EXPIRATION)
        .build()

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()

    val pendingIntent = getGeofencePendingIntent(context, reminder.id)

    geofencingClient.addGeofences(geofencingRequest, pendingIntent)

    return true
}

fun removeGeofences(context: Context, geofences: List<Geofence>) {
    val geofencingClient = LocationServices.getGeofencingClient(context)

    geofencingClient.removeGeofences(geofences.map { it.requestId })
}