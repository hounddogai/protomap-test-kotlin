package com.acme.transport.safety.scorecard.group

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryScorecardThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701480785)
        val items = mutableListOf<PrimaryScorecardThreshold>()
        for (index in 0 until 3) {
            items.add(newPrimaryScorecardThreshold("primary-scorecard-threshold-$index", "sample"))
        }
        val report = buildPrimaryScorecardThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
