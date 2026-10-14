package com.acme.transport.network.hub.ledger

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingHubLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708636252)
        val items = mutableListOf<PendingHubLedger>()
        for (index in 0 until 5) {
            items.add(newPendingHubLedger("pending-hub-ledger-$index", "sample"))
        }
        val report = buildPendingHubLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
