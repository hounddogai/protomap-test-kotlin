package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newNodeLedger("code-632", "label-335")
        assertEquals(NodeLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newNodeLedger("code-632", "label-335")
        try {
            item.moveTo(NodeLedgerStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeLedgerStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newNodeLedger("code-632", "label-335")
        item.status = NodeLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(NodeLedgerStatus.IDLE) }
    }
}
