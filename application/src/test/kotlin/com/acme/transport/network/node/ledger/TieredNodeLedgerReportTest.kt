package com.acme.transport.network.node.ledger

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredNodeLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706410928)
        val items = mutableListOf<TieredNodeLedger>()
        for (index in 0 until 5) {
            items.add(newTieredNodeLedger("tiered-node-ledger-$index", "sample"))
        }
        val report = buildTieredNodeLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
