package com.acme.transport.linehaul.trailer.trailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTrailerBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTrailerBatch("code-498", "label-978")
        val view = toBatchedTrailerBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTrailerBatchViews(mutableListOf<BatchedTrailerBatch>(item, item)).size, "views")
    }
}
