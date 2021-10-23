package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.projec4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.util.MainCoroutineRule
import com.udacity.project4.util.asDataItem
import com.udacity.project4.util.assertReminder
import com.udacity.project4.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun setup() {

        dataSource = FakeDataSource(mutableListOf())
        viewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

    }

    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    fun liveDataIsCleared() {
        // GIVEN - View Model's live data is filled
        viewModel.reminderTitle.value = "Coliseu"
        viewModel.reminderDescription.value = "È il mio cavallo di battaglia"
        viewModel.reminderSelectedLocationStr.value = "Colosseum"
        viewModel.latitude.value = 41.890728
        viewModel.longitude.value = 12.492222
        viewModel.selectedPOI.value =
            PointOfInterest(LatLng(41.890728, 12.492222), "Coliseu", "Coliseu")

        // WHEN - Live data is cleared
        viewModel.onClear()

        // THEN - Live data must be null
        assert(viewModel.reminderTitle.getOrAwaitValue() == null)
        assert(viewModel.reminderDescription.getOrAwaitValue() == null)
        assert(viewModel.reminderSelectedLocationStr.getOrAwaitValue() == null)
        assert(viewModel.latitude.getOrAwaitValue() == null)
        assert(viewModel.longitude.getOrAwaitValue() == null)
        assert(viewModel.selectedPOI.getOrAwaitValue() == null)

    }

    @Test
    fun reminderIsSaved() {
        // GIVEN - A Reminder Data Item to be save
        val reminder = ReminderDataItem(
            "Cristo Redentor",
            "Comer aquela feijoada",
            "Christ the Redeemer",
            -22.951944,
            -43.210556,
            "2"
        )

        // WHEN - Request to save it to data source
        viewModel.saveReminder(reminder)

        // THEN - It must be saved as is
        val got = runBlocking { dataSource.getReminder(reminder.id) }
        assert(got is Result.Success)
        got as Result.Success

        assertReminder(reminder, got.data.asDataItem())

    }

    @Test
    fun dataIsCorrectValidated() {
        // GIVEN - A valid data item
        var dataItem = ReminderDataItem(
            "Cristo Redentor",
            "Comer aquela feijoada",
            "Christ the Redeemer",
            -22.951944,
            -43.210556,
            "2"
        )
        // WHEN - Try to validate it
        var result = viewModel.validateEnteredData(dataItem)

        // THEN - Should return true and showSnackBarInt should be null
        assert(result)
        assert(viewModel.showSnackBar.value == null)

        // GIVEN - A data item with invalid title
        dataItem = ReminderDataItem(
            null,
            "Comer aquela feijoada",
            "Christ the Redeemer",
            -22.951944,
            -43.210556,
            "2"
        )

        // WHEN - Try to validate it
        result = viewModel.validateEnteredData(dataItem)

        // THEN - Should return false and showSnackBarInt should be R.string.err_enter_title
        assert(!result)
        assert(viewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_enter_title)


        // GIVEN - A data item with invalid location
        dataItem = ReminderDataItem(
            "Cristo Redentor",
            "Comer aquela feijoada",
            null,
            -22.951944,
            -43.210556,
            "2"
        )

        // WHEN - Try to validate it
        result = viewModel.validateEnteredData(dataItem)

        // THEN - Should return false and showSnackBarInt should be R.string.err_select_location
        assert(!result)
        assert(viewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_select_location)

    }

    @Test
    fun shouldSaveValidReminder() {
        // GIVEN - A valid data item
        val reminder = ReminderDataItem(
            "Cristo Redentor",
            "Comer aquela feijoada",
            "Christ the Redeemer",
            -22.951944,
            -43.210556,
            "2"
        )

        // WHEN - Call validateAndSaveReminder
        viewModel.validateAndSaveReminder(reminder)

        // THEN -  Should save
        val got = runBlocking { dataSource.getReminder(reminder.id) }
        assert(got is Result.Success)
        got as Result.Success

        assertReminder(reminder, got.data.asDataItem())
    }

    @Test
    fun shouldNotSaveInvalidReminder() {
        // GIVEN - A invalid data item
        val reminderNoTitle = ReminderDataItem(
            null,
            "Comer aquela feijoada",
            "Christ the Redeemer",
            -22.951944,
            -43.210556,
            "2"
        )

        val reminderNoLocation = ReminderDataItem(
            "Taj Mahal",
            "Rawdah-i munawwarah",
            null,
            27.175,
            78.041944,
            "3"
        )

        // WHEN - Call validateAndSaveReminder
        viewModel.validateAndSaveReminder(reminderNoTitle)
        viewModel.validateAndSaveReminder(reminderNoLocation)

        // THEN -  Should not save
        var got = runBlocking { dataSource.getReminder(reminderNoTitle.id) }
        assert(got is Result.Error)

        got = runBlocking { dataSource.getReminder(reminderNoLocation.id) }
        assert(got is Result.Error)
    }


}