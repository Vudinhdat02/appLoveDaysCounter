package com.example.lovedays.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.lovedays.core.time.ClockProvider
import com.example.lovedays.core.time.SystemClockProvider
import com.example.lovedays.data.db.AppDatabase
import com.example.lovedays.data.db.NotificationLogDao
import com.example.lovedays.data.db.SpecialDayDao
import com.example.lovedays.notification.NotificationHelper
import com.example.lovedays.notification.ReminderNotifier
import com.example.lovedays.widget.GlanceWidgetRefresher
import com.example.lovedays.widget.WidgetRefresher
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("couple_settings") })

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            // Thêm Migration vào đây mỗi khi tăng version của AppDatabase.
            .build()

    @Provides
    fun provideSpecialDayDao(db: AppDatabase): SpecialDayDao = db.specialDayDao()

    @Provides
    fun provideNotificationLogDao(db: AppDatabase): NotificationLogDao = db.notificationLogDao()

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {
    @Binds
    abstract fun bindClock(impl: SystemClockProvider): ClockProvider

    @Binds
    abstract fun bindNotifier(impl: NotificationHelper): ReminderNotifier

    @Binds
    abstract fun bindWidgetRefresher(impl: GlanceWidgetRefresher): WidgetRefresher
}
