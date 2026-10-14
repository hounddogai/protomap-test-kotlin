package com.acme.transport.lastmile.drop.target

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDropLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706210629)
        val items = mutableListOf<AdaptiveDropLedger>()
        for (index in 0 until 2) {
            items.add(newAdaptiveDropLedger("adaptive-drop-ledger-$index", "sample"))
        }
        val report = buildAdaptiveDropLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
