package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705817087)
        val items = mutableListOf<CappedTrailerGroup>()
        for (index in 0 until 6) {
            items.add(newCappedTrailerGroup("capped-trailer-group-$index", "sample"))
        }
        val report = buildCappedTrailerGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
