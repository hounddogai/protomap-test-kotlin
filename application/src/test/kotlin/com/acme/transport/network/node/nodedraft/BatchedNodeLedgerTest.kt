package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNodeLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedNodeLedger("code-118", "title-937")
        assertEquals(BatchedNodeLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedNodeLedger("code-118", "title-937")
        try {
            item.moveTo(BatchedNodeLedgerStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNodeLedgerStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedNodeLedger("code-118", "title-937")
        item.status = BatchedNodeLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedNodeLedgerStatus.PLANNED) }
    }
}
