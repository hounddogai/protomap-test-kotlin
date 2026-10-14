package com.acme.transport.network.node.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyNodeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyNodeBatch("code-231", "title-801")
        val view = toLegacyNodeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyNodeBatchViews(mutableListOf<LegacyNodeBatch>(item, item)).size, "views")
    }
}
