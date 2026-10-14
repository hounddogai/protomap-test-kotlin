package com.acme.transport.safety.incident.group

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentBatch("code-358", "label-894")
        val view = toIncidentBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentBatchViews(mutableListOf<IncidentBatch>(item, item)).size, "views")
    }
}
