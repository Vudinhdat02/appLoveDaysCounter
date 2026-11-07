package com.example.lovedays.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lovedays.domain.SpecialDay
import java.time.LocalDate

@Entity(tableName = "special_days")
data class SpecialDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateEpochDay: Long,
    val repeatYearly: Boolean,
    val emoji: String = SpecialDay.DEFAULT_EMOJI,
    val remindEnabled: Boolean = true,
    val createdAt: Long,
)

@Entity(
    tableName = "notification_log",
    primaryKeys = ["eventKey", "triggerType", "eventDateEpochDay"],
)
data class NotificationLogEntity(
    val eventKey: String,
    val triggerType: String,
    val eventDateEpochDay: Long,
    val sentAt: Long,
)

fun SpecialDayEntity.toDomain() = SpecialDay(
    id = id,
    title = title,
    date = LocalDate.ofEpochDay(dateEpochDay),
    repeatYearly = repeatYearly,
    emoji = emoji,
    remindEnabled = remindEnabled,
    createdAt = createdAt,
)

fun SpecialDay.toEntity() = SpecialDayEntity(
    id = id,
    title = title.trim(),
    dateEpochDay = date.toEpochDay(),
    repeatYearly = repeatYearly,
    emoji = emoji,
    remindEnabled = remindEnabled,
    createdAt = createdAt,
)
