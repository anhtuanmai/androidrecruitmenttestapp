package fr.leboncoin.core.data.utils

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AnalyticsHelperTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var analyticsHelper: AnalyticsHelper

    @Before
    fun setUp() {
        context = mockk()
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor

        analyticsHelper = AnalyticsHelper(context)
    }

    @Test
    fun `trackSelection should write to shared preferences`() {
        // Given
        val itemId = "test_item"

        // When
        analyticsHelper.trackSelection(itemId)

        // Then
        verify { editor.putString(any(), eq(itemId)) }
    }
}
