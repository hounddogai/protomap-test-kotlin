package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerPreference("code-143", "label-268")
        val view = toTrailerPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerPreferenceViews(mutableListOf<TrailerPreference>(item, item)).size, "views")
    }
}
