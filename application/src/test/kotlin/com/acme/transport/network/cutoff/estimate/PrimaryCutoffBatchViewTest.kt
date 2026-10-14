package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCutoffBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCutoffBatch("code-90", "name-376")
        val view = toPrimaryCutoffBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCutoffBatchViews(mutableListOf<PrimaryCutoffBatch>(item, item)).size, "views")
    }
}
