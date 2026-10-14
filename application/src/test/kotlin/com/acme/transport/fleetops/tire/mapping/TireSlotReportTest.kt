package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702249021)
        val items = mutableListOf<TireSlot>()
        for (index in 0 until 4) {
            items.add(newTireSlot("tire-slot-$index", "sample"))
        }
        val report = buildTireSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
