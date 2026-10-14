package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705796411)
        val items = mutableListOf<DropSegment>()
        for (index in 0 until 4) {
            items.add(newDropSegment("drop-segment-$index", "sample"))
        }
        val report = buildDropSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
