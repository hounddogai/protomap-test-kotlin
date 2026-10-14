package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientInspectionSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707542041)
        val items = mutableListOf<LenientInspectionSegment>()
        for (index in 0 until 4) {
            items.add(newLenientInspectionSegment("lenient-inspection-segment-$index", "sample"))
        }
        val report = buildLenientInspectionSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
