package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDisputeEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDisputeEstimate("code-92", "name-177")
        val view = toBatchedDisputeEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDisputeEstimateViews(mutableListOf<BatchedDisputeEstimate>(item, item)).size, "views")
    }
}
