package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedNodeLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedNodeLedger("code-118", "title-937")
        val view = toBatchedNodeLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedNodeLedgerViews(mutableListOf<BatchedNodeLedger>(item, item)).size, "views")
    }
}
