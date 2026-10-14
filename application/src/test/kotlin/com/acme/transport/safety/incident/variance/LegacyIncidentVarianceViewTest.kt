package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyIncidentVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyIncidentVariance("code-164", "label-645")
        val view = toLegacyIncidentVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyIncidentVarianceViews(mutableListOf<LegacyIncidentVariance>(item, item)).size, "views")
    }
}
