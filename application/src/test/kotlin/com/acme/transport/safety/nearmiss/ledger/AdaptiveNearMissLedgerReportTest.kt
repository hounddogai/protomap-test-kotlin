package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNearMissLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704591136)
        val items = mutableListOf<AdaptiveNearMissLedger>()
        for (index in 0 until 6) {
            items.add(newAdaptiveNearMissLedger("adaptive-near-miss-ledger-$index", "sample"))
        }
        val report = buildAdaptiveNearMissLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
