package com.acme.transport.safety.incident.incidentproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientIncidentAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientIncidentAdjustment("code-413", "name-775")
        val view = toLenientIncidentAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientIncidentAdjustmentViews(mutableListOf<LenientIncidentAdjustment>(item, item)).size, "views")
    }
}
