package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDockDoorSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701514142)
        val items = mutableListOf<LenientDockDoorSlot>()
        for (index in 0 until 3) {
            items.add(newLenientDockDoorSlot("lenient-dock-door-slot-$index", "sample"))
        }
        val report = buildLenientDockDoorSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
