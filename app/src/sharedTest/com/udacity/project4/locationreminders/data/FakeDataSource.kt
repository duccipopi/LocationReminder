package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private val data: MutableList<ReminderDTO> = mutableListOf(
        ReminderDTO(
            "Coliseu",
            "È il mio cavallo di battaglia",
            "Colosseum",
            41.890278,
            12.492222,
            "1"
        ),
        ReminderDTO(
            "Cristo Redentor",
            "Comer aquela feijoada",
            "Christ the Redeemer",
            -22.951944,
            -43.210556,
            "2"
        ),
        ReminderDTO(
            "Taj Mahal",
            "Rawdah-i munawwarah",
            "Taj Mahal",
            27.175,
            78.041944,
            "3"
        )
    )
) : ReminderDataSource {

    private var shouldReturnError = false
    private var errorMessage = "No reminder with given id found"

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError)
            Result.Error(errorMessage)
        else
            Result.Success(data)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        data.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = data.find { id == it.id }
        return if (reminder == null || shouldReturnError)
            Result.Error(errorMessage)
        else
            Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
        data.clear()
    }

    fun alwaysReturnError(value: Boolean, message: String = "Always return error") {
        shouldReturnError = value
        errorMessage = message
    }


}