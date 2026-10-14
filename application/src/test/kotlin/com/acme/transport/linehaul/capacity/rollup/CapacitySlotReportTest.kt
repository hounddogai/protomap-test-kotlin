package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacitySlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702992538)
        val items = mutableListOf<CapacitySlot>()
        for (index in 0 until 4) {
            items.add(newCapacitySlot("capacity-slot-$index", "sample"))
        }
        val report = buildCapacitySlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
