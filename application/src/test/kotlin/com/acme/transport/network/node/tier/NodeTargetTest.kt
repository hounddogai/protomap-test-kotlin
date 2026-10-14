package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeTargetTest {
    @Test
    fun `starts in open`() {
        val item = newNodeTarget("code-740", "name-262")
        assertEquals(NodeTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newNodeTarget("code-740", "name-262")
        try {
            item.moveTo(NodeTargetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeTargetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newNodeTarget("code-740", "name-262")
        item.status = NodeTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(NodeTargetStatus.OPEN) }
    }
}
