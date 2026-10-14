package com.acme.transport.safety.incident.incidentbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedIncidentBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedIncidentBatch("code-451", "label-681")
        val view = toWeightedIncidentBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedIncidentBatchViews(mutableListOf<WeightedIncidentBatch>(item, item)).size, "views")
    }
}
