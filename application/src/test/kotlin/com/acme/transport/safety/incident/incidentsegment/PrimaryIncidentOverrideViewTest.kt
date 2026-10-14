package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryIncidentOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryIncidentOverride("code-820", "title-318")
        val view = toPrimaryIncidentOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryIncidentOverrideViews(mutableListOf<PrimaryIncidentOverride>(item, item)).size, "views")
    }
}
