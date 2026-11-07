package com.example.lovedays.data.repository

import com.example.lovedays.core.time.ClockProvider
import com.example.lovedays.data.db.SpecialDayDao
import com.example.lovedays.data.db.toDomain
import com.example.lovedays.data.db.toEntity
import com.example.lovedays.domain.SpecialDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpecialDayRepository @Inject constructor(
    private val dao: SpecialDayDao,
    private val clock: ClockProvider,
) {
    val specialDays: Flow<List<SpecialDay>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getAll(): List<SpecialDay> = dao.getAll().map { it.toDomain() }

    suspend fun get(id: Long): SpecialDay? = dao.getById(id)?.toDomain()

    /** Thêm mới nếu id = 0, ngược lại cập nhật. Trả về id. */
    suspend fun save(day: SpecialDay): Long =
        if (day.id == 0L) {
            dao.insert(day.copy(createdAt = clock.now().toEpochMilli()).toEntity())
        } else {
            dao.update(day.toEntity())
            day.id
        }

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
