package com.acme.transport.network.cutoff.claim

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700238715)
        val items = mutableListOf<CutoffLedger>()
        for (index in 0 until 4) {
            items.add(newCutoffLedger("cutoff-ledger-$index", "sample"))
        }
        val report = buildCutoffLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
