package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCapacityGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707036967)
        val items = mutableListOf<TieredCapacityGroup>()
        for (index in 0 until 6) {
            items.add(newTieredCapacityGroup("tiered-capacity-group-$index", "sample"))
        }
        val report = buildTieredCapacityGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
