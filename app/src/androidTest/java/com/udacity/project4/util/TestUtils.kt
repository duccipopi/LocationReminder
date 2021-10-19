package com.udacity.project4.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

fun assertReminder(reference: ReminderDTO, given: ReminderDTO?) {
    assert(given != null)
    given?.let {
        assert(it.id == reference.id)
        assert(it.description == reference.description)
        assert(it.location == reference.location)
        assert(it.latitude == reference.latitude)
        assert(it.longitude == reference.longitude)
    }

}