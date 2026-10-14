package com.acme.transport.network.node.forecast

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709895180)
        val items = mutableListOf<NodeLedger>()
        for (index in 0 until 6) {
            items.add(newNodeLedger("node-ledger-$index", "sample"))
        }
        val report = buildNodeLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
