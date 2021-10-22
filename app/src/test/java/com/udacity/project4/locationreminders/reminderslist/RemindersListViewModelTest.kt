package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.util.MainCoroutineRule
import com.udacity.project4.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setup() {

        dataSource = FakeDataSource()
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())

    }

    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    fun beforeLoadReminders() {
        // GIVEN - Data Source has entries
        // WHEN - Load reminders is not called
        // THEN - List must be empty
        assert(viewModel.remindersList.value.isNullOrEmpty())

        // THEN - Loading and No Data states must be false
        assert(viewModel.showNoData.value == null)


    }

    @Test
    fun afterLoadingReminders() {
        // GIVEN - View model loads data
        // WHEN - Load reminders is called
        viewModel.loadReminders()

        // THEN - List must have values, no data must be false
        assert(viewModel.remindersList.getOrAwaitValue().isNotEmpty())
        assert(!viewModel.showNoData.getOrAwaitValue())

    }

    @Test
    fun noDataLoaded() {
        // GIVEN - Data Source has no entry
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), FakeDataSource(
                mutableListOf()
            )
        )

        // WHEN - Load reminders is called
        viewModel.loadReminders()

        // THEN - List must be empty and no data must be true
        assert(viewModel.remindersList.getOrAwaitValue().isNullOrEmpty())
        assert(viewModel.showNoData.getOrAwaitValue())


    }

    // TODO shouldReturnError and check_loading

}
