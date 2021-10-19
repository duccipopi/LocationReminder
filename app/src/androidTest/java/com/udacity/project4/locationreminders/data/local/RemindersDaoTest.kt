package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao

    private fun assertReminder(reference: ReminderDTO, given: ReminderDTO?) {
        assert(given != null)
        given?.let {
            assert(it.id == reference.id)
            assert(it.description == reference.description)
            assert(it.location == reference.location)
            assert(it.latitude == reference.latitude)
            assert(it.longitude == reference.longitude)
        }

    }

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.reminderDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun emptyDB() = runBlockingTest {
        // GIVEN - No reminder was added in DB

        // WHEN - Get the reminder list
        val entries = dao.getReminders()

        // THEN - None should be returned
        assert(entries.isEmpty())
    }

    @Test
    fun insertAndGet() = runBlockingTest {
        // GIVEN - One reminder is inserted
        val reminder = ReminderDTO(
            "Coliseu",
            "È il mio cavallo di battaglia",
            "Colosseum",
            41.890278,
            12.492222,
            "1"
        )

        dao.saveReminder(reminder)

        // WHEN - Get it by id
        val got = dao.getReminderById(reminder.id)

        // THEN - The data should return as is
        assertReminder(reminder, got)

    }

    @Test
    fun insertManyAndGetAsMany() = runBlockingTest {
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

        for (reminder in data) dao.saveReminder(reminder)

        // WHEN - Get all reminder
        val loaded = dao.getReminders()

        // THEN - All should be returned as is
        assert(data.size == loaded.size)

        data.forEach { reference ->
            val reminder = loaded.find { it.id == reference.id }
            assertReminder(reference, reminder)
        }


    }

    @Test
    fun clearDB() = runBlockingTest {
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

        for (reminder in data) dao.saveReminder(reminder)

        // WHEN - All reminder are deleted
        dao.deleteAllReminders()

        // THEN - No reminder should remain
        val got = dao.getReminders()

        assert(got.isNullOrEmpty())
    }


}