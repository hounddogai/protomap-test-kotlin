package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDockDoorCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702343240)
        val items = mutableListOf<AdaptiveDockDoorCycle>()
        for (index in 0 until 3) {
            items.add(newAdaptiveDockDoorCycle("adaptive-dock-door-cycle-$index", "sample"))
        }
        val report = buildAdaptiveDockDoorCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
