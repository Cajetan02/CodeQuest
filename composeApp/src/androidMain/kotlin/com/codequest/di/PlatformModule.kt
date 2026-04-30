package com.codequest.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single {
        val context = androidContext()
        createDataStore(context)
    }
}

private fun createDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        File(context.filesDir, "datastore/user_prefs.preferences_pb").apply {
            parentFile?.mkdirs()
        }.absolutePath.toPath()
    }
}
