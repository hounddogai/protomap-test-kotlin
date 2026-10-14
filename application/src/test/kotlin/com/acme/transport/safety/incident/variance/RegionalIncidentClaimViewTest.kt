package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalIncidentClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalIncidentClaim("code-519", "name-624")
        val view = toRegionalIncidentClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalIncidentClaimViews(mutableListOf<RegionalIncidentClaim>(item, item)).size, "views")
    }
}
