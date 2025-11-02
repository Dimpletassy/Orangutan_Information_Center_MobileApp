package com.oic.myapplication

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

//@RunWith(AndroidJUnit4::class)
//class ExampleUnitTest {
//    @Test
//    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)
//    }
//    private val dbController = databaseController()
//
//    @Test
//    fun getDailyLogForToday() {
//        val latch = CountDownLatch(1)
//        var dailyLogData: Map<String, Any>? = null
//
//        dbController.getDailyLog("2025-09-30") { data ->
//            dailyLogData = data
//            latch.countDown()
//        }
//
//        // Wait for the callback (max 5 seconds)
//        val completed = latch.await(5, TimeUnit.SECONDS)
//        assertTrue("Firestore callback timed out", completed)
//
//        // Assert that document exists
//        assertNotNull("Daily log should exist", dailyLogData)
//
//        // Example: Assert the date field matches
//        val dateField = dailyLogData?.get("date") as? String
//        assertEquals("2025-09-30", dateField)
//
//        // Example: Assert logs contains at least one entry
//        val logs = dailyLogData?.get("logs") as? Map<*, *>
//        assertNotNull("Logs should not be null", logs)
//        assertTrue("Logs should have at least one entry", logs!!.isNotEmpty())
//    }
//}