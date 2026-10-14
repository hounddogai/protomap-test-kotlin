package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationBatch("code-491", "title-219")
        val view = toUtilizationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationBatchViews(mutableListOf<UtilizationBatch>(item, item)).size, "views")
    }
}
