package com.acme.transport.fleetops.tire.segment

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTireSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706632007)
        val items = mutableListOf<WeightedTireSegment>()
        for (index in 0 until 6) {
            items.add(newWeightedTireSegment("weighted-tire-segment-$index", "sample"))
        }
        val report = buildWeightedTireSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
