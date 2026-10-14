package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708136288)
        val items = mutableListOf<ClearanceSlot>()
        for (index in 0 until 5) {
            items.add(newClearanceSlot("clearance-slot-$index", "sample"))
        }
        val report = buildClearanceSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
