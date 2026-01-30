package fr.leboncoin.core.data.utils

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun trackSelection(itemId: String) {
        val prefs = context.getSharedPreferences(ANALYTICS_SHARED_PREFS, Context.MODE_PRIVATE)
        prefs.edit { putString(SELECTED_ITEM_KEY, itemId) }
        println("Analytics: User selected item - $itemId")
    }

    fun trackScreenView(screenName: String) {
        println("Analytics: Screen viewed - $screenName")
    }
}

private const val ANALYTICS_SHARED_PREFS = "analytics_prefs"
private const val SELECTED_ITEM_KEY = "selected_item"
