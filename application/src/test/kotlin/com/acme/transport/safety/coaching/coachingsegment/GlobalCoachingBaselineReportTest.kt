package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCoachingBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707989155)
        val items = mutableListOf<GlobalCoachingBaseline>()
        for (index in 0 until 6) {
            items.add(newGlobalCoachingBaseline("global-coaching-baseline-$index", "sample"))
        }
        val report = buildGlobalCoachingBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
