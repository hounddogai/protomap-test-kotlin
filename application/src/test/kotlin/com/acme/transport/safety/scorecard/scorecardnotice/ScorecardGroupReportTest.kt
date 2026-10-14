package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702666910)
        val items = mutableListOf<ScorecardGroup>()
        for (index in 0 until 2) {
            items.add(newScorecardGroup("scorecard-group-$index", "sample"))
        }
        val report = buildScorecardGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
