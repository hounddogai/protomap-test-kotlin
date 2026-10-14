package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffPreference("code-442", "title-77")
        val view = toCutoffPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffPreferenceViews(mutableListOf<CutoffPreference>(item, item)).size, "views")
    }
}
