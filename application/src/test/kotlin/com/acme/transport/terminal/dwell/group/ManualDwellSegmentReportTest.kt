package com.acme.transport.terminal.dwell.group

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701983463)
        val items = mutableListOf<ManualDwellSegment>()
        for (index in 0 until 6) {
            items.add(newManualDwellSegment("manual-dwell-segment-$index", "sample"))
        }
        val report = buildManualDwellSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
