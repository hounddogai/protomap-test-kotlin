package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCapacitySlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702737198)
        val items = mutableListOf<FallbackCapacitySlot>()
        for (index in 0 until 5) {
            items.add(newFallbackCapacitySlot("fallback-capacity-slot-$index", "sample"))
        }
        val report = buildFallbackCapacitySlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
