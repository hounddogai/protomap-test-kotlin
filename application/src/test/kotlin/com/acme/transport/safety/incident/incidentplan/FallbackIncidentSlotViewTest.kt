package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackIncidentSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackIncidentSlot("code-848", "title-404")
        val view = toFallbackIncidentSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackIncidentSlotViews(mutableListOf<FallbackIncidentSlot>(item, item)).size, "views")
    }
}
