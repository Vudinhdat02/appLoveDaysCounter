package com.example.lovedays.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Phiên bản 1. Mỗi lần đổi schema phải tăng version và thêm một `Migration` rõ ràng
 * (schema được xuất ra thư mục app/schemas).
 */
@Database(
    entities = [SpecialDayEntity::class, NotificationLogEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun specialDayDao(): SpecialDayDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        const val NAME = "love_days.db"
    }
}
