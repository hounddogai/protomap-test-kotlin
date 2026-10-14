package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeRollupTest {
    @Test
    fun `starts in open`() {
        val item = newNodeRollup("code-384", "label-536")
        assertEquals(NodeRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newNodeRollup("code-384", "label-536")
        try {
            item.moveTo(NodeRollupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeRollupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newNodeRollup("code-384", "label-536")
        item.status = NodeRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(NodeRollupStatus.OPEN) }
    }
}
