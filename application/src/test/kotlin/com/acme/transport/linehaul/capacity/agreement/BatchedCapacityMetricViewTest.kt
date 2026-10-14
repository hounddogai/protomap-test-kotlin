package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacityMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacityMetric("code-61", "title-410")
        val view = toBatchedCapacityMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacityMetricViews(mutableListOf<BatchedCapacityMetric>(item, item)).size, "views")
    }
}
