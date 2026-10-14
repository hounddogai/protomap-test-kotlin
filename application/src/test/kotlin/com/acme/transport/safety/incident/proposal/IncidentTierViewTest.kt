package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentTier("code-285", "label-178")
        val view = toIncidentTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentTierViews(mutableListOf<IncidentTier>(item, item)).size, "views")
    }
}
