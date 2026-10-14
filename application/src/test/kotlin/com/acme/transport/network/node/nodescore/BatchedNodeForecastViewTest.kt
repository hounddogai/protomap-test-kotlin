package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedNodeForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedNodeForecast("code-298", "title-852")
        val view = toBatchedNodeForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedNodeForecastViews(mutableListOf<BatchedNodeForecast>(item, item)).size, "views")
    }
}
