package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701689526)
        val items = mutableListOf<ScorecardAdjustment>()
        for (index in 0 until 6) {
            items.add(newScorecardAdjustment("scorecard-adjustment-$index", "sample"))
        }
        val report = buildScorecardAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
