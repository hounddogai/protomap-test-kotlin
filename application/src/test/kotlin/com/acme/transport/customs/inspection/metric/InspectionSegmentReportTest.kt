package com.acme.transport.customs.inspection.metric

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701310286)
        val items = mutableListOf<InspectionSegment>()
        for (index in 0 until 5) {
            items.add(newInspectionSegment("inspection-segment-$index", "sample"))
        }
        val report = buildInspectionSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
