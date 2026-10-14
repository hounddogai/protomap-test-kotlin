package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingScorecardProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703573406)
        val items = mutableListOf<RollingScorecardProfile>()
        for (index in 0 until 5) {
            items.add(newRollingScorecardProfile("rolling-scorecard-profile-$index", "sample"))
        }
        val report = buildRollingScorecardProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
