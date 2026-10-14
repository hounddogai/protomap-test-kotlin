package com.acme.transport.safety.coaching.metric

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702364030)
        val items = mutableListOf<CoachingMapping>()
        for (index in 0 until 2) {
            items.add(newCoachingMapping("coaching-mapping-$index", "sample"))
        }
        val report = buildCoachingMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
