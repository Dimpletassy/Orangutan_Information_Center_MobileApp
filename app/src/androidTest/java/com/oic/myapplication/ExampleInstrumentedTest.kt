package com.oic.myapplication

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.oic.myapplication.services.database.DatabaseController
import com.oic.myapplication.services.database.models.*
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private val db = DatabaseController()
    private val TIMEOUT = 10L // seconds
    private val auth by lazy { FirebaseAuth.getInstance() }


    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.oic.myapplication", appContext.packageName)
    }

    /**
     * Assumes there is a DailyLog doc for this date in test data
     */
    @Test
    fun getDailyLog_forKnownDate_returnsDoc() {
        val latch = CountDownLatch(1)
        val ref = AtomicReference<Map<String, Any>?>()

        db.getDailyLog("2025-10-30") { data ->
            ref.set(data)
            latch.countDown()
        }

        assertTrue("Firestore callback timed out", latch.await(TIMEOUT, TimeUnit.SECONDS))
        val doc = ref.get()
        assertNotNull("Daily log should exist", doc)
        assertEquals("2025-10-30", doc!!["date"] as? String)

        val logs = doc["logs"] as? Map<*, *>
        assertNotNull("Logs should not be null", logs)
        assertTrue("Logs should have at least one entry", logs!!.isNotEmpty())
    }

    /**
     * Begin a manual run and verify the realtime listener reflects running=true.
     * Cleans up by setting running=false.
     */
    @Test
    fun beginManualRun_thenListener_reportsRunningTrue() {
        val latch = CountDownLatch(1)
        val lastRunning = AtomicReference<Boolean?>(null)
        var reg: ListenerRegistration? = null

        try {
            reg = db.listenActiveManualRun { doc ->
                lastRunning.set(doc?.running)
                if (doc?.running == true) latch.countDown()
            }

            // Start a small manual run
            val startLatch = CountDownLatch(1)
            db.beginManualRun(totalLitres = 10) { startLatch.countDown() }
            assertTrue("beginManualRun timed out", startLatch.await(TIMEOUT, TimeUnit.SECONDS))

            // Wait until listener sees running=true
            assertTrue("Listener didn't observe running=true", latch.await(TIMEOUT, TimeUnit.SECONDS))
            assertEquals(true, lastRunning.get())
        } finally {
            // Cleanup: end manual run
            val endLatch = CountDownLatch(1)
            db.endManualRun { endLatch.countDown() }
            endLatch.await(TIMEOUT, TimeUnit.SECONDS)
            reg?.remove()
        }
    }

    /**
     * End a manual run and verify the listener reflects running=false.
     */
    @Test
    fun endManualRun_listenerReportsFalse() {
        val latch = CountDownLatch(1)
        var reg: ListenerRegistration? = null

        try {
            // Ensure we start first so we can observe stop
            val started = CountDownLatch(1)
            db.beginManualRun(10) { started.countDown() }
            assertTrue("beginManualRun timed out", started.await(TIMEOUT, TimeUnit.SECONDS))

            reg = db.listenActiveManualRun { doc ->
                if (doc?.running == false) latch.countDown()
            }

            val endLatch = CountDownLatch(1)
            db.endManualRun { endLatch.countDown() }
            assertTrue("endManualRun timed out", endLatch.await(TIMEOUT, TimeUnit.SECONDS))

            assertTrue("Listener didn't observe running=false", latch.await(TIMEOUT, TimeUnit.SECONDS))
        } finally {
            reg?.remove()
        }
    }

    /**
     * Upsert a schedule period and read it back, verifying selected fields.
     * Uses MONDAY + MORNING; safe since set(..., merge=true) won’t wipe other periods.
     */
    @Test
    fun upsertSchedulePeriod_thenGetScheduleDay_roundTrip() {
        val done = CountDownLatch(1)
        val retrieved = AtomicReference<ScheduleDay?>()

        val entry = ScheduleEntry(
            enabled = true,
            litres = 20,
            startTime = "07:00" // stored string; your UI can parse as needed
        )

        db.upsertSchedulePeriod(
            day = Day.MONDAY,
            period = Period.MORNING,
            entry = entry
        ) { upsertRes ->
            assertTrue("Upsert failed: ${upsertRes.exceptionOrNull()}", upsertRes.isSuccess)

            db.getScheduleDay(Day.MONDAY) { getRes ->
                assertTrue("Get failed: ${getRes.exceptionOrNull()}", getRes.isSuccess)
                retrieved.set(getRes.getOrNull())
                done.countDown()
            }
        }

        assertTrue("Firestore ops timed out", done.await(TIMEOUT, TimeUnit.SECONDS))
        val dayDoc = retrieved.get()
        assertNotNull("ScheduleDay should exist", dayDoc)
        assertEquals(Day.MONDAY, dayDoc!!.day)
        assertNotNull("Morning entry should exist", dayDoc.morning)
        assertEquals(20, dayDoc.morning!!.litres)
        assertEquals(true, dayDoc.morning!!.enabled)
        assertEquals("07:00", dayDoc.morning!!.startTime)
    }

    /**
     * Delete a schedule period and verify it’s removed on read.
     * We target MIDDAY to avoid clobbering MORNING used elsewhere.
     */
    @Test
    fun deleteSchedulePeriod_removesField() {
        val done = CountDownLatch(1)
        val after = AtomicReference<ScheduleDay?>()

        // First write a MIDDAY entry to be sure it exists
        val seed = CountDownLatch(1)
        db.upsertSchedulePeriod(
            day = Day.MONDAY,
            period = Period.MIDDAY,
            entry = ScheduleEntry(enabled = true, litres = 15, startTime = "12:30")
        ) { seed.countDown() }
        assertTrue("Seed upsert timed out", seed.await(TIMEOUT, TimeUnit.SECONDS))

        // Now delete it and verify
        db.deleteSchedulePeriod(day = Day.MONDAY, period = Period.MIDDAY) { delRes ->
            assertTrue("Delete failed: ${delRes.exceptionOrNull()}", delRes.isSuccess)
            db.getScheduleDay(Day.MONDAY) { getRes ->
                assertTrue("Get failed: ${getRes.exceptionOrNull()}", getRes.isSuccess)
                after.set(getRes.getOrNull())
                done.countDown()
            }
        }

        assertTrue("Firestore ops timed out", done.await(TIMEOUT, TimeUnit.SECONDS))
        val dayDoc = after.get()
        // After deletion, .midday should be null
        assertTrue("MIDDAY should be removed (null)", dayDoc?.midday == null)
    }
}
