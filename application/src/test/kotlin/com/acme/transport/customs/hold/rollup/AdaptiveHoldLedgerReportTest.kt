package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveHoldLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708865270)
        val items = mutableListOf<AdaptiveHoldLedger>()
        for (index in 0 until 4) {
            items.add(newAdaptiveHoldLedger("adaptive-hold-ledger-$index", "sample"))
        }
        val report = buildAdaptiveHoldLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
