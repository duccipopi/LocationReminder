package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.material.snackbar.SnackbarContentLayout
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var appContext: Application
    private lateinit var repository: ReminderDataSource

    @Before
    fun setup() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { FakeDataSource(mutableListOf()) as ReminderDataSource }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        repository = get()
    }

    @Test
    fun navigateToAddReminder() = runBlocking {
        // GIVEN - On Reminder list fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN -  Click in FAB to add a new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify it has navigated to SaveReminderFragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )

    }

    @Test
    fun reminderIsCorrectlyDisplayed() = runBlockingTest {
        // GIVEN - A reminder is added to repository
        val reminder = ReminderDTO(
            "Taj Mahal",
            "Rawdah-i munawwarah",
            "Taj Mahal",
            27.175,
            78.041944,
            "3"
        )
        repository.saveReminder(reminder)

        // WHEN - List fragment launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - The reminder is correctly displayed
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderssRecyclerView)).check(matches(hasDescendant((withText(reminder.title)))))
        onView(withId(R.id.reminderssRecyclerView)).check(matches(hasDescendant((withText(reminder.description)))))
        onView(withId(R.id.reminderssRecyclerView)).check(matches(hasDescendant((withText(reminder.location)))))

    }

    @Test
    fun checkForErrorMessages() = runBlockingTest {
        // GIVEN - A failed data source
        val message = "0xF417ED"
        (repository as FakeDataSource).alwaysReturnError(true, message)

        // WHEN - List fragment launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - An error message should be displayed in a Snackbar
        onView(withText(message)).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )
        onView(withClassName(containsString(SnackbarContentLayout::class.qualifiedName))).check(
            matches(hasDescendant(withText(message)))
        )

    }
}