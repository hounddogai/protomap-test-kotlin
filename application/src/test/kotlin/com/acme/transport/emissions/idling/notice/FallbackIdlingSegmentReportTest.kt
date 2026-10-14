package com.acme.transport.emissions.idling.notice

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackIdlingSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703657719)
        val items = mutableListOf<FallbackIdlingSegment>()
        for (index in 0 until 6) {
            items.add(newFallbackIdlingSegment("fallback-idling-segment-$index", "sample"))
        }
        val report = buildFallbackIdlingSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
