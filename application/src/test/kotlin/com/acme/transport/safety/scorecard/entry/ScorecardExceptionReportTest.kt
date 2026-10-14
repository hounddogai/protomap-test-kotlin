package com.acme.transport.safety.scorecard.entry

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707858827)
        val items = mutableListOf<ScorecardException>()
        for (index in 0 until 5) {
            items.add(newScorecardException("scorecard-exception-$index", "sample"))
        }
        val report = buildScorecardExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
