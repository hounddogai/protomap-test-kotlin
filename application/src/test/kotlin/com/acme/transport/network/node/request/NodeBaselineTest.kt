package com.acme.transport.network.node.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newNodeBaseline("code-214", "name-746")
        assertEquals(NodeBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newNodeBaseline("code-214", "name-746")
        try {
            item.moveTo(NodeBaselineStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeBaselineStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newNodeBaseline("code-214", "name-746")
        item.status = NodeBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(NodeBaselineStatus.DRAFT) }
    }
}
