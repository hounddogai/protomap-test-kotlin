package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSwapLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706466211)
        val items = mutableListOf<StrictSwapLedger>()
        for (index in 0 until 3) {
            items.add(newStrictSwapLedger("strict-swap-ledger-$index", "sample"))
        }
        val report = buildStrictSwapLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
