package com.acme.transport.safety.incident.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalIncidentBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalIncidentBatch("code-151", "title-561")
        val view = toGlobalIncidentBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalIncidentBatchViews(mutableListOf<GlobalIncidentBatch>(item, item)).size, "views")
    }
}
