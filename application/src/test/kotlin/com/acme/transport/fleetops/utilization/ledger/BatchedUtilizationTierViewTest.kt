package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedUtilizationTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedUtilizationTier("code-311", "label-583")
        val view = toBatchedUtilizationTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedUtilizationTierViews(mutableListOf<BatchedUtilizationTier>(item, item)).size, "views")
    }
}
