package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyIncidentEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyIncidentEntry("code-850", "label-51")
        val view = toLegacyIncidentEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyIncidentEntryViews(mutableListOf<LegacyIncidentEntry>(item, item)).size, "views")
    }
}
