package com.acme.transport.emissions.evcharge.batchedchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingEvChargeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingEvChargeBatch("code-479", "label-319")
        val view = toPendingEvChargeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingEvChargeBatchViews(mutableListOf<PendingEvChargeBatch>(item, item)).size, "views")
    }
}
