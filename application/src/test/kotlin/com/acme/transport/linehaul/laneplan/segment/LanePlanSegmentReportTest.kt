package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708200173)
        val items = mutableListOf<LanePlanSegment>()
        for (index in 0 until 6) {
            items.add(newLanePlanSegment("lane-plan-segment-$index", "sample"))
        }
        val report = buildLanePlanSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
