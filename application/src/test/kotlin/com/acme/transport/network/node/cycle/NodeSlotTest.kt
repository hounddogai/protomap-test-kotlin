package com.acme.transport.network.node.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newNodeSlot("code-680", "label-824")
        assertEquals(NodeSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newNodeSlot("code-680", "label-824")
        try {
            item.moveTo(NodeSlotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeSlotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newNodeSlot("code-680", "label-824")
        item.status = NodeSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(NodeSlotStatus.REQUESTED) }
    }
}
