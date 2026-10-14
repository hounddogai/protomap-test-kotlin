package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedIncidentSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedIncidentSlot("code-721", "title-290")
        val view = toCappedIncidentSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedIncidentSlotViews(mutableListOf<CappedIncidentSlot>(item, item)).size, "views")
    }
}
