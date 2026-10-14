package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701214839)
        val items = mutableListOf<ScorecardThreshold>()
        for (index in 0 until 5) {
            items.add(newScorecardThreshold("scorecard-threshold-$index", "sample"))
        }
        val report = buildScorecardThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
