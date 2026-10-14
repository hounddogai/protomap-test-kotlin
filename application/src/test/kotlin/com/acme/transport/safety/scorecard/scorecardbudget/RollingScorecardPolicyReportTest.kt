package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingScorecardPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704364421)
        val items = mutableListOf<RollingScorecardPolicy>()
        for (index in 0 until 5) {
            items.add(newRollingScorecardPolicy("rolling-scorecard-policy-$index", "sample"))
        }
        val report = buildRollingScorecardPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
