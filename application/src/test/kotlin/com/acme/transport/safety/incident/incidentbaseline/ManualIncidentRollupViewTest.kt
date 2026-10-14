package com.acme.transport.safety.incident.incidentbaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualIncidentRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualIncidentRollup("code-9", "title-424")
        val view = toManualIncidentRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualIncidentRollupViews(mutableListOf<ManualIncidentRollup>(item, item)).size, "views")
    }
}
