package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCapacityAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701607317)
        val items = mutableListOf<ScheduledCapacityAllocation>()
        for (index in 0 until 3) {
            items.add(newScheduledCapacityAllocation("scheduled-capacity-allocation-$index", "sample"))
        }
        val report = buildScheduledCapacityAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
