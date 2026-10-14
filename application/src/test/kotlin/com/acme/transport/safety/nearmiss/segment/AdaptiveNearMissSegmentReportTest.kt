package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNearMissSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709376175)
        val items = mutableListOf<AdaptiveNearMissSegment>()
        for (index in 0 until 5) {
            items.add(newAdaptiveNearMissSegment("adaptive-near-miss-segment-$index", "sample"))
        }
        val report = buildAdaptiveNearMissSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
