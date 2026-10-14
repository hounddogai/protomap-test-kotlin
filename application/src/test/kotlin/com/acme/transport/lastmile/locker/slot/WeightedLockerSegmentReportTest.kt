package com.acme.transport.lastmile.locker.slot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLockerSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704727327)
        val items = mutableListOf<WeightedLockerSegment>()
        for (index in 0 until 2) {
            items.add(newWeightedLockerSegment("weighted-locker-segment-$index", "sample"))
        }
        val report = buildWeightedLockerSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
