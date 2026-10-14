package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDisputeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDisputeBatch("code-409", "label-437")
        val view = toBatchedDisputeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDisputeBatchViews(mutableListOf<BatchedDisputeBatch>(item, item)).size, "views")
    }
}
