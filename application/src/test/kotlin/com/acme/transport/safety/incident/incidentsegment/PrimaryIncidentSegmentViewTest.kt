package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryIncidentSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryIncidentSegment("code-824", "title-316")
        val view = toPrimaryIncidentSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryIncidentSegmentViews(mutableListOf<PrimaryIncidentSegment>(item, item)).size, "views")
    }
}
