package com.example.lovedays.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.lovedays.data.db.AppDatabase
import com.example.lovedays.data.db.NotificationLogEntity
import com.example.lovedays.data.db.SpecialDayEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun specialDay_crud() = runTest {
        val dao = db.specialDayDao()
        val id = dao.insert(SpecialDayEntity(title = "Sinh nhật", dateEpochDay = 10_000, repeatYearly = true, createdAt = 1))
        dao.observeAll().test {
            assertEquals(listOf("Sinh nhật"), awaitItem().map { it.title })

            val saved = dao.getById(id)!!
            dao.update(saved.copy(title = "Sinh nhật em", emoji = "🎂", remindEnabled = false))
            val updated = awaitItem().single()
            assertEquals("Sinh nhật em", updated.title)
            assertEquals("🎂", updated.emoji)
            assertEquals(false, updated.remindEnabled)

            dao.deleteById(id)
            assertEquals(emptyList<SpecialDayEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(dao.getById(id))
    }

    @Test
    fun notificationLog_rejectsDuplicates() = runTest {
        val dao = db.notificationLogDao()
        val log = NotificationLogEntity("milestone_days_500", "SAME_DAY", 20_000, sentAt = 1)
        assertNotEquals(-1L, dao.insert(log))
        assertEquals(-1L, dao.insert(log.copy(sentAt = 2)))
        // Khác loại nhắc thì vẫn được lưu.
        assertNotEquals(-1L, dao.insert(log.copy(triggerType = "ONE_DAY_BEFORE")))
        assertEquals(2, dao.getAll().size)

        dao.deleteOlderThan(20_001)
        assertEquals(0, dao.getAll().size)
    }
}
