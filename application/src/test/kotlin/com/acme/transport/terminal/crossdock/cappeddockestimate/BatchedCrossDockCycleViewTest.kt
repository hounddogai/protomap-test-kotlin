package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCrossDockCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCrossDockCycle("code-984", "label-237")
        val view = toBatchedCrossDockCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCrossDockCycleViews(mutableListOf<BatchedCrossDockCycle>(item, item)).size, "views")
    }
}
