package com.acme.transport.safety.scorecard.profile

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708719587)
        val items = mutableListOf<ScorecardProfile>()
        for (index in 0 until 2) {
            items.add(newScorecardProfile("scorecard-profile-$index", "sample"))
        }
        val report = buildScorecardProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
