package com.acme.transport.crewing.swap.mapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalSwapSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703419638)
        val items = mutableListOf<SeasonalSwapSegment>()
        for (index in 0 until 2) {
            items.add(newSeasonalSwapSegment("seasonal-swap-segment-$index", "sample"))
        }
        val report = buildSeasonalSwapSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
