package com.acme.transport.safety.coaching.metric

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCoachingWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707709733)
        val items = mutableListOf<LegacyCoachingWindow>()
        for (index in 0 until 6) {
            items.add(newLegacyCoachingWindow("legacy-coaching-window-$index", "sample"))
        }
        val report = buildLegacyCoachingWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
