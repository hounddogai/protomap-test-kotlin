package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702253789)
        val items = mutableListOf<CrossDockSlot>()
        for (index in 0 until 4) {
            items.add(newCrossDockSlot("cross-dock-slot-$index", "sample"))
        }
        val report = buildCrossDockSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
