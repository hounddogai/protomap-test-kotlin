package com.acme.transport.safety.violation.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalViolationSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701429977)
        val items = mutableListOf<GlobalViolationSegment>()
        for (index in 0 until 2) {
            items.add(newGlobalViolationSegment("global-violation-segment-$index", "sample"))
        }
        val report = buildGlobalViolationSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
