package com.udacity.project4.util

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun assertReminder(reference: ReminderDTO, given: ReminderDTO?) {
    assert(given != null)
    given?.let {
        assert(it.id == reference.id)
        assert(it.title == reference.title)
        assert(it.description == reference.description)
        assert(it.location == reference.location)
        assert(it.latitude == reference.latitude)
        assert(it.longitude == reference.longitude)
    }
}

fun assertReminder(reference: ReminderDataItem, given: ReminderDataItem?) {
    assert(given != null)
    given?.let {
        assert(it.id == reference.id)
        assert(it.title == reference.title)
        assert(it.description == reference.description)
        assert(it.location == reference.location)
        assert(it.latitude == reference.latitude)
        assert(it.longitude == reference.longitude)
    }
}

// From https://classroom.udacity.com/nanodegrees/nd940/parts/1920b2b2-488a-402c-922e-719b852afe63/modules/c09fa0a0-6a90-4803-96e2-6fc597437fe4/lessons/460d0aff-091b-4278-901f-8da3784762be/concepts/ec513b03-4268-49bd-9a1d-cb6bb8c5faa9
// For a full explanation of what this class, check out this https://medium.com/androiddevelopers/unit-testing-livedata-and-other-common-observability-problems-bb477262eb04
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> MutableLiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    try {
        afterObserve.invoke()

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

    } finally {
        this.removeObserver(observer)
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun ReminderDTO.asDataItem(): ReminderDataItem {
    return ReminderDataItem(
        title, description, location, latitude, longitude, id
    )
}

@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) :
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher) {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}