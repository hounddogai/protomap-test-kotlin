package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedEvChargeScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedEvChargeScore("code-567", "name-141")
        val view = toBatchedEvChargeScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedEvChargeScoreViews(mutableListOf<BatchedEvChargeScore>(item, item)).size, "views")
    }
}
