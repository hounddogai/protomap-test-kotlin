package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRelayBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRelayBatch("code-486", "name-704")
        val view = toBatchedRelayBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRelayBatchViews(mutableListOf<BatchedRelayBatch>(item, item)).size, "views")
    }
}
