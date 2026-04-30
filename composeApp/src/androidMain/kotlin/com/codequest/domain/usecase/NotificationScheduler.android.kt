package com.codequest.domain.usecase

actual class NotificationScheduler {
    actual fun scheduleDailyReminder() {
        // In a complete implementation, Android WorkManager is enqueued here.
        // E.g., WorkManager.getInstance(context).enqueueUniquePeriodicWork(...)
    }
}
