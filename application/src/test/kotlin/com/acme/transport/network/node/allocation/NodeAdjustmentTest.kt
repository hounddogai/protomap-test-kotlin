package com.acme.transport.network.node.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newNodeAdjustment("code-740", "title-768")
        assertEquals(NodeAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newNodeAdjustment("code-740", "title-768")
        try {
            item.moveTo(NodeAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newNodeAdjustment("code-740", "title-768")
        item.status = NodeAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(NodeAdjustmentStatus.NEW) }
    }
}
