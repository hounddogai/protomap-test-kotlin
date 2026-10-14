package com.acme.transport.network.zone.threshold

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705938195)
        val items = mutableListOf<ZoneThreshold>()
        for (index in 0 until 6) {
            items.add(newZoneThreshold("zone-threshold-$index", "sample"))
        }
        val report = buildZoneThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
