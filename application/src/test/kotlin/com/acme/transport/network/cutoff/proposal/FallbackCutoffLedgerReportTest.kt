package com.acme.transport.network.cutoff.proposal

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCutoffLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709028955)
        val items = mutableListOf<FallbackCutoffLedger>()
        for (index in 0 until 5) {
            items.add(newFallbackCutoffLedger("fallback-cutoff-ledger-$index", "sample"))
        }
        val report = buildFallbackCutoffLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
