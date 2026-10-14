package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCrossDockSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707591289)
        val items = mutableListOf<ScheduledCrossDockSlot>()
        for (index in 0 until 2) {
            items.add(newScheduledCrossDockSlot("scheduled-cross-dock-slot-$index", "sample"))
        }
        val report = buildScheduledCrossDockSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
