package com.example.lovedays.core.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * Context có áp dụng ngôn ngữ riêng của app (per-app language). Từ Android 13 hệ thống tự áp dụng;
 * với bản cũ hơn, worker/widget/thông báo chạy ngoài Activity nên cần tự tạo context theo ngôn ngữ.
 */
fun localizedContext(context: Context): Context {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return context
    val locales = AppCompatDelegate.getApplicationLocales()
    if (locales.isEmpty) return context
    val locale: Locale = locales[0] ?: return context
    val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
    return context.createConfigurationContext(config)
}
