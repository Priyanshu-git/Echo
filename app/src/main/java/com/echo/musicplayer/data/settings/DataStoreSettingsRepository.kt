package com.echo.musicplayer.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.echo.musicplayer.domain.model.AppSettings
import com.echo.musicplayer.domain.model.ThemeMode
import com.echo.musicplayer.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val SETTINGS_DATASTORE_NAME = "echo_settings"

private val Context.settingsDataStore by preferencesDataStore(name = SETTINGS_DATASTORE_NAME)

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            AppSettings(
                primaryColorArgb = preferences[Keys.PrimaryColorArgb] ?: AppSettings().primaryColorArgb,
                themeMode = preferences[Keys.ThemeMode]?.let { value ->
                    runCatching { ThemeMode.valueOf(value) }.getOrDefault(AppSettings().themeMode)
                } ?: AppSettings().themeMode,
                keepScreenOnWhilePlaying = preferences[Keys.KeepScreenOnWhilePlaying] ?: AppSettings().keepScreenOnWhilePlaying,
            )
        }

    override suspend fun setPrimaryColor(argb: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.PrimaryColorArgb] = argb
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.ThemeMode] = mode.name
        }
    }

    override suspend fun setKeepScreenOnWhilePlaying(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.KeepScreenOnWhilePlaying] = enabled
        }
    }

    private object Keys {
        val PrimaryColorArgb = longPreferencesKey("primary_color_argb")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val KeepScreenOnWhilePlaying = booleanPreferencesKey("keep_screen_on_while_playing")
    }
}
