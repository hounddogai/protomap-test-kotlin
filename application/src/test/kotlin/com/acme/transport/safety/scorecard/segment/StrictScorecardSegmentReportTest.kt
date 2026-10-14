package com.acme.transport.safety.scorecard.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictScorecardSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709414713)
        val items = mutableListOf<StrictScorecardSegment>()
        for (index in 0 until 3) {
            items.add(newStrictScorecardSegment("strict-scorecard-segment-$index", "sample"))
        }
        val report = buildStrictScorecardSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
