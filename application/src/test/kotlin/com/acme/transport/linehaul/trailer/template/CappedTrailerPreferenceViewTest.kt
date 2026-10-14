package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedTrailerPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedTrailerPreference("code-538", "title-809")
        val view = toCappedTrailerPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedTrailerPreferenceViews(mutableListOf<CappedTrailerPreference>(item, item)).size, "views")
    }
}
