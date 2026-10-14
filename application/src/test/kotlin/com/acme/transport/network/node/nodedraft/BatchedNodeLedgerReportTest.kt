package com.acme.transport.network.node.nodedraft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNodeLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707953016)
        val items = mutableListOf<BatchedNodeLedger>()
        for (index in 0 until 3) {
            items.add(newBatchedNodeLedger("batched-node-ledger-$index", "sample"))
        }
        val report = buildBatchedNodeLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
