package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.assertReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun emptyDB() = runBlocking {
        // GIVEN - No reminder was added in DB

        // WHEN - Get the reminder list
        val result = repository.getReminders()

        // THEN - None should be returned, but with Success
        assert(result is Result.Success)
        result as Result.Success
        assert(result.data.isEmpty())
    }

    @Test
    fun insertAndGet() = runBlocking {
        // GIVEN - One reminder is inserted
        val reminder = ReminderDTO(
            "Coliseu",
            "È il mio cavallo di battaglia",
            "Colosseum",
            41.890278,
            12.492222,
            "1"
        )

        repository.saveReminder(reminder)

        // WHEN - Get it by id
        val got = repository.getReminder(reminder.id)

        // THEN - The data should return as is
        assert(got is Result.Success)
        got as Result.Success
        assertReminder(reminder, got.data)

    }

    @Test
    fun getInvalidReminder() = runBlocking {
        // GIVEN - a non inserted reminder id
        val invalidId = "!!!INVALID!!!"

        // WHEN - tries to get it
        val got = repository.getReminder(invalidId)

        // THEN - return a fail
        assert(got is Result.Error)

    }

    @Test
    fun insertManyAndGetAsMany() = runBlocking {
        // GIVEN - Many reminders are added
        val data = mutableListOf(
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

        for (reminder in data) repository.saveReminder(reminder)

        // WHEN - Get all reminder
        val loaded = repository.getReminders()

        // THEN - All should be returned as is
        assert(loaded is Result.Success)
        loaded as Result.Success
        assert(data.size == loaded.data.size)

        data.forEach { reference ->
            val reminder = loaded.data.find { it.id == reference.id }
            assertReminder(reference, reminder)
        }


    }

    @Test
    fun clearDB() = runBlocking {
        // GIVEN - Many reminders are added
        val data = mutableListOf(
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

        for (reminder in data) repository.saveReminder(reminder)

        // WHEN - All reminder are deleted
        repository.deleteAllReminders()

        // THEN - No reminder should remain
        val got = repository.getReminders()

        assert(got is Result.Success)
        got as Result.Success
        assert(got.data.isNullOrEmpty())
    }

}