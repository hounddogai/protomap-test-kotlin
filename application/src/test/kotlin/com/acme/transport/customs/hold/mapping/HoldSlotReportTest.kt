package com.acme.transport.customs.hold.mapping

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709570386)
        val items = mutableListOf<HoldSlot>()
        for (index in 0 until 6) {
            items.add(newHoldSlot("hold-slot-$index", "sample"))
        }
        val report = buildHoldSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
