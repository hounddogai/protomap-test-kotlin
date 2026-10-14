package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newNodeWindow("code-503", "name-829")
        assertEquals(NodeWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newNodeWindow("code-503", "name-829")
        try {
            item.moveTo(NodeWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newNodeWindow("code-503", "name-829")
        item.status = NodeWindowStatus.CLOSED
        assertFails("move") { item.moveTo(NodeWindowStatus.DRAFT) }
    }
}
