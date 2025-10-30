package com.oic.myapplication

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oic.myapplication.services.database.DatabaseController

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.oic.myapplication", appContext.packageName)
    }

    private val dbController = DatabaseController()

    @Test
    fun getDailyLogForToday() {
        val latch = CountDownLatch(1)
        var dailyLogData: Map<String, Any>? = null

        dbController.getDailyLog("2025-09-30") { data ->
            dailyLogData = data
            latch.countDown()
        }

        // Wait for the callback (max 5 seconds)
        val completed = latch.await(5, TimeUnit.SECONDS)
        assertTrue("Firestore callback timed out", completed)

        // Assert that document exists
        assertNotNull("Daily log should exist", dailyLogData)

        // Example: Assert the date field matches
        val dateField = dailyLogData?.get("date") as? String
        assertEquals("2025-09-30", dateField)

        // Example: Assert logs contains at least one entry
        val logs = dailyLogData?.get("logs") as? Map<*, *>
        assertNotNull("Logs should not be null", logs)
        assertTrue("Logs should have at least one entry", logs!!.isNotEmpty())
    }


}