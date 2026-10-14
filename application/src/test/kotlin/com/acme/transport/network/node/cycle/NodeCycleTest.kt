package com.acme.transport.network.node.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newNodeCycle("code-348", "title-895")
        assertEquals(NodeCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newNodeCycle("code-348", "title-895")
        try {
            item.moveTo(NodeCycleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeCycleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newNodeCycle("code-348", "title-895")
        item.status = NodeCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(NodeCycleStatus.PROPOSED) }
    }
}
