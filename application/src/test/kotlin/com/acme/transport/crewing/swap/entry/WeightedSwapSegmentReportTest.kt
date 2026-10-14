package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedSwapSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709031440)
        val items = mutableListOf<WeightedSwapSegment>()
        for (index in 0 until 2) {
            items.add(newWeightedSwapSegment("weighted-swap-segment-$index", "sample"))
        }
        val report = buildWeightedSwapSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
