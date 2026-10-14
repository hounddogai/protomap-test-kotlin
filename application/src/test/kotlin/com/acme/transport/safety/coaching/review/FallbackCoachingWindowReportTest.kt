package com.acme.transport.safety.coaching.review

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCoachingWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703258846)
        val items = mutableListOf<FallbackCoachingWindow>()
        for (index in 0 until 6) {
            items.add(newFallbackCoachingWindow("fallback-coaching-window-$index", "sample"))
        }
        val report = buildFallbackCoachingWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
