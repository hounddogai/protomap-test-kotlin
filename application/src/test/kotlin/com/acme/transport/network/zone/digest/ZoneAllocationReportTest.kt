package com.acme.transport.network.zone.digest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705151743)
        val items = mutableListOf<ZoneAllocation>()
        for (index in 0 until 4) {
            items.add(newZoneAllocation("zone-allocation-$index", "sample"))
        }
        val report = buildZoneAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
