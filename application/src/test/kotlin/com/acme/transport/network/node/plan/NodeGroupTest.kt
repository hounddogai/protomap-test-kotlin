package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeGroup("code-718", "label-322")
        assertEquals(NodeGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newNodeGroup("code-718", "label-322")
        try {
            item.moveTo(NodeGroupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeGroupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeGroup("code-718", "label-322")
        item.status = NodeGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeGroupStatus.PLANNED) }
    }
}
