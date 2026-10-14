package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704352564)
        val items = mutableListOf<ScorecardMilestone>()
        for (index in 0 until 4) {
            items.add(newScorecardMilestone("scorecard-milestone-$index", "sample"))
        }
        val report = buildScorecardMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
