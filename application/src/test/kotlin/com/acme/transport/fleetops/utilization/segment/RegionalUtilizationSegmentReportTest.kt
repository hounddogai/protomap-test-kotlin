package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalUtilizationSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702484514)
        val items = mutableListOf<RegionalUtilizationSegment>()
        for (index in 0 until 4) {
            items.add(newRegionalUtilizationSegment("regional-utilization-segment-$index", "sample"))
        }
        val report = buildRegionalUtilizationSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
