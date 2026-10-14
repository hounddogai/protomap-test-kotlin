package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTenderEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTenderEstimate("code-112", "title-450")
        val view = toBatchedTenderEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTenderEstimateViews(mutableListOf<BatchedTenderEstimate>(item, item)).size, "views")
    }
}
