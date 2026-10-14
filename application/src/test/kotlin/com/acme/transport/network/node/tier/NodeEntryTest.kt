package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeEntry("code-185", "name-905")
        assertEquals(NodeEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newNodeEntry("code-185", "name-905")
        try {
            item.moveTo(NodeEntryStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeEntryStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeEntry("code-185", "name-905")
        item.status = NodeEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeEntryStatus.PLANNED) }
    }
}
