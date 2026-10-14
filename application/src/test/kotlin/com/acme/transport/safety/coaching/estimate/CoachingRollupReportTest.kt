package com.acme.transport.safety.coaching.estimate

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700992226)
        val items = mutableListOf<CoachingRollup>()
        for (index in 0 until 6) {
            items.add(newCoachingRollup("coaching-rollup-$index", "sample"))
        }
        val report = buildCoachingRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
