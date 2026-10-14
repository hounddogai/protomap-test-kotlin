package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTransitTimeRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTransitTimeRequest("code-571", "label-926")
        val view = toBatchedTransitTimeRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTransitTimeRequestViews(mutableListOf<BatchedTransitTimeRequest>(item, item)).size, "views")
    }
}
