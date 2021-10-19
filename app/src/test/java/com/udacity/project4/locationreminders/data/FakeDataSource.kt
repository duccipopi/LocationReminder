package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private val data = mutableListOf(
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

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(data)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        data.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = data.find { id == it.id }
        return if (reminder != null)
            Result.Success(reminder)
        else
            Result.Error("No reminder with $id found")
    }

    override suspend fun deleteAllReminders() {
        data.clear()
    }


}