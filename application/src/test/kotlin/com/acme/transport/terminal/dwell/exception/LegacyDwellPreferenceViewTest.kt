package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDwellPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDwellPreference("code-579", "name-199")
        val view = toLegacyDwellPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDwellPreferenceViews(mutableListOf<LegacyDwellPreference>(item, item)).size, "views")
    }
}
