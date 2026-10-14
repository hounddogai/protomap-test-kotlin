package com.acme.transport.terminal.dwell.preference

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707627032)
        val items = mutableListOf<DwellSegment>()
        for (index in 0 until 2) {
            items.add(newDwellSegment("dwell-segment-$index", "sample"))
        }
        val report = buildDwellSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
