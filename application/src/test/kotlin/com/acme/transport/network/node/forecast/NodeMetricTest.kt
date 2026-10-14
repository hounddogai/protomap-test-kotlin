package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newNodeMetric("code-320", "name-770")
        assertEquals(NodeMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newNodeMetric("code-320", "name-770")
        try {
            item.moveTo(NodeMetricStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeMetricStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newNodeMetric("code-320", "name-770")
        item.status = NodeMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(NodeMetricStatus.PROPOSED) }
    }
}
