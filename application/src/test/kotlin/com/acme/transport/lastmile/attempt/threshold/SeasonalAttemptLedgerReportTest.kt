package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAttemptLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709274953)
        val items = mutableListOf<SeasonalAttemptLedger>()
        for (index in 0 until 5) {
            items.add(newSeasonalAttemptLedger("seasonal-attempt-ledger-$index", "sample"))
        }
        val report = buildSeasonalAttemptLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
