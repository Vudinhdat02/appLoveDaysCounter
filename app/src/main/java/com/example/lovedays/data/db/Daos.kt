package com.example.lovedays.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialDayDao {
    @Query("SELECT * FROM special_days ORDER BY dateEpochDay ASC")
    fun observeAll(): Flow<List<SpecialDayEntity>>

    @Query("SELECT * FROM special_days ORDER BY dateEpochDay ASC")
    suspend fun getAll(): List<SpecialDayEntity>

    @Query("SELECT * FROM special_days WHERE id = :id")
    suspend fun getById(id: Long): SpecialDayEntity?

    @Insert
    suspend fun insert(entity: SpecialDayEntity): Long

    @Update
    suspend fun update(entity: SpecialDayEntity)

    @Delete
    suspend fun delete(entity: SpecialDayEntity)

    @Query("DELETE FROM special_days WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM special_days")
    suspend fun deleteAll()
}

@Dao
interface NotificationLogDao {
    /** Trả về -1 nếu bản ghi đã tồn tại (đã gửi rồi). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: NotificationLogEntity): Long

    @Query("SELECT * FROM notification_log")
    suspend fun getAll(): List<NotificationLogEntity>

    @Query("DELETE FROM notification_log WHERE eventDateEpochDay < :epochDay")
    suspend fun deleteOlderThan(epochDay: Long)

    @Query("DELETE FROM notification_log")
    suspend fun deleteAll()
}
