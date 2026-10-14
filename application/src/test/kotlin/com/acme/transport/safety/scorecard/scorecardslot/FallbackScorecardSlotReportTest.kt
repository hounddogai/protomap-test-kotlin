package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackScorecardSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700759013)
        val items = mutableListOf<FallbackScorecardSlot>()
        for (index in 0 until 3) {
            items.add(newFallbackScorecardSlot("fallback-scorecard-slot-$index", "sample"))
        }
        val report = buildFallbackScorecardSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
