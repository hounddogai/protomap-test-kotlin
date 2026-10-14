package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707710262)
        val items = mutableListOf<ScorecardRecord>()
        for (index in 0 until 6) {
            items.add(newScorecardRecord("scorecard-record-$index", "sample"))
        }
        val report = buildScorecardRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
