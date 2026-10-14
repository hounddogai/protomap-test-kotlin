package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newNodeForecast("code-482", "label-124")
        assertEquals(NodeForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newNodeForecast("code-482", "label-124")
        try {
            item.moveTo(NodeForecastStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeForecastStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newNodeForecast("code-482", "label-124")
        item.status = NodeForecastStatus.CLOSED
        assertFails("move") { item.moveTo(NodeForecastStatus.DRAFT) }
    }
}
