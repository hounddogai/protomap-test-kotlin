package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledZoneSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706737169)
        val items = mutableListOf<ScheduledZoneSegment>()
        for (index in 0 until 5) {
            items.add(newScheduledZoneSegment("scheduled-zone-segment-$index", "sample"))
        }
        val report = buildScheduledZoneSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
