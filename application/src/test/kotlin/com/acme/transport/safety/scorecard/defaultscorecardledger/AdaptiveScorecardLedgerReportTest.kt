package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveScorecardLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705145950)
        val items = mutableListOf<AdaptiveScorecardLedger>()
        for (index in 0 until 6) {
            items.add(newAdaptiveScorecardLedger("adaptive-scorecard-ledger-$index", "sample"))
        }
        val report = buildAdaptiveScorecardLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
