package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyIncidentPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyIncidentPreference("code-163", "label-14")
        val view = toLegacyIncidentPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyIncidentPreferenceViews(mutableListOf<LegacyIncidentPreference>(item, item)).size, "views")
    }
}
