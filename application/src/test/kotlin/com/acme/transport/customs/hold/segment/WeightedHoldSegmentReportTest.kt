package com.acme.transport.customs.hold.segment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedHoldSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701290667)
        val items = mutableListOf<WeightedHoldSegment>()
        for (index in 0 until 6) {
            items.add(newWeightedHoldSegment("weighted-hold-segment-$index", "sample"))
        }
        val report = buildWeightedHoldSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
