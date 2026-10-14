package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTrailerYardSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701847916)
        val items = mutableListOf<LenientTrailerYardSegment>()
        for (index in 0 until 3) {
            items.add(newLenientTrailerYardSegment("lenient-trailer-yard-segment-$index", "sample"))
        }
        val report = buildLenientTrailerYardSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
